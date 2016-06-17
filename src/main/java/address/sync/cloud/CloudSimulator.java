package address.sync.cloud;

import address.sync.cloud.model.CloudAddressBook;
import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;
import address.exceptions.DataConversionException;
import address.util.AppLogger;
import address.util.LoggerManager;

import java.io.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This class is responsible for emulating a cloud with an API similar
 * to a subset of GitHub's, together with a given hourly API quota.
 *
 * Requests for a full list of objects should be done in pages. Responses
 * will include first page/prev page/next page/last page if they exist.
 *
 * Any bad requests due to inappropriate parameters will still consume API
 * usage.
 *
 * In addition, data returned by this cloud may be modified due to
 * simulated corruption or its responses may have significant delays,
 * if the cloud is initialized with an unreliable network parameter
 */
public class CloudSimulator implements ICloudSimulator {
    private static final AppLogger logger = LoggerManager.getLogger(CloudSimulator.class);
    private static final int API_QUOTA_PER_HOUR = 5000;
    private static final Random RANDOM_GENERATOR = new Random();
    private static final double FAILURE_PROBABILITY = 0.1;
    private static final double NETWORK_DELAY_PROBABILITY = 0.2;
    private static final int MIN_DELAY_IN_SEC = 1;
    private static final int DELAY_RANGE = 5;
    private static final double MODIFY_PERSON_PROBABILITY = 0.1;
    private static final double MODIFY_TAG_PROBABILITY = 0.05;
    private static final double ADD_PERSON_PROBABILITY = 0.05;
    private static final double ADD_TAG_PROBABILITY = 0.025;
    private static final int MAX_NUM_PERSONS_TO_ADD = 2;
    private CloudRateLimitStatus cloudRateLimitStatus;
    private boolean shouldSimulateUnreliableNetwork;
    private CloudFileHandler fileHandler;

    public CloudSimulator(CloudFileHandler fileHandler, CloudRateLimitStatus cloudRateLimitStatus,
                          boolean shouldSimulateUnreliableNetwork) {
        this.fileHandler = fileHandler;
        this.cloudRateLimitStatus = cloudRateLimitStatus;
        this.shouldSimulateUnreliableNetwork = shouldSimulateUnreliableNetwork;
    }

    public CloudSimulator(boolean shouldSimulateUnreliableNetwork) {
        fileHandler = new CloudFileHandler();
        cloudRateLimitStatus = new CloudRateLimitStatus(API_QUOTA_PER_HOUR);
        this.shouldSimulateUnreliableNetwork = shouldSimulateUnreliableNetwork;
        cloudRateLimitStatus.restartQuotaTimer();
    }

    /**
     * Attempts to create a person if quota is available
     *
     * A new ID for the new person will be generated; and the ID field in the given newPerson will be ignored
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param newPerson
     * @return a response wrapper, containing the added person if successful
     */
    @Override
    public CloudResponse createPerson(String addressBookName, CloudPerson newPerson, String previousETag) {
        logger.debug("createPerson called with: addressbook {}, person {}, prevETag {}", addressBookName, newPerson, previousETag);
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);

        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            CloudPerson returnedPerson = addPerson(fileData.getAllPersons(), newPerson);
            fileHandler.writeCloudAddressBookToFile(fileData);

            modifyCloudPersonBasedOnChance(returnedPerson);

            CloudResponse cloudResponse = new CloudResponse(HttpURLConnection.HTTP_CREATED, returnedPerson, getHeaders(cloudRateLimitStatus));
            String eTag = getResponseETag(cloudResponse);
            if (eTag.equals(previousETag)) return getNotModifiedResponse();

            cloudRateLimitStatus.useQuota(1);
            return cloudResponse;
        } catch (IllegalArgumentException e) {
            cloudRateLimitStatus.useQuota(1);
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Returns a response wrapper containing the list of persons if quota is available
     * <p>
     * Consumes 1 + floor(persons size/resourcesPerPage) API usage
     *
     * @param addressBookName
     * @param resourcesPerPage
     * @return
     */
    @Override
    public CloudResponse getPersons(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
        logger.debug("getPersons called with: addressbook {}, page {}, resourcesperpage {}, prevETag {}", addressBookName, pageNumber, resourcesPerPage, previousETag);
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        List<CloudPerson> fullPersonList = new ArrayList<>();
        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            fullPersonList.addAll(fileData.getAllPersons());
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);

        List<CloudPerson> queryResults = getQueryResults(pageNumber, resourcesPerPage, fullPersonList);

        mutateCloudPersonList(queryResults);

        CloudResponse contentResponse = new CloudResponse(HttpURLConnection.HTTP_OK, queryResults, getHeaders(cloudRateLimitStatus));
        String eTag = getResponseETag(contentResponse);
        if (eTag.equals(previousETag)) return getNotModifiedResponse();

        cloudRateLimitStatus.useQuota(1);

        if (isValidPageNumber(fullPersonList.size(), pageNumber, resourcesPerPage)) {
            fillInPageNumbers(pageNumber, resourcesPerPage, fullPersonList, contentResponse);
        }
        return contentResponse;
    }

    /**
     * Returns a response wrapper containing the list of tags if quota is available
     * <p>
     * Consumes 1 + floor(tag list/resourcesPerPage) API usage
     *
     * @param addressBookName
     * @param resourcesPerPage
     * @return
     */
    @Override
    public CloudResponse getTags(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
        logger.debug("getTags called with: addressbook {}, page {}, resourcesperpage {}, prevETag {}", addressBookName, pageNumber, resourcesPerPage, previousETag);
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        List<CloudTag> fullTagList = new ArrayList<>();

        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            fullTagList.addAll(fileData.getAllTags());
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        List<CloudTag> queryResults = getQueryResults(pageNumber, resourcesPerPage, fullTagList);
        modifyCloudTagListBasedOnChance(queryResults);

        CloudResponse contentResponse = new CloudResponse(HttpURLConnection.HTTP_OK, queryResults, getHeaders(cloudRateLimitStatus));
        String eTag = getResponseETag(contentResponse);
        if (eTag.equals(previousETag)) return getNotModifiedResponse();

        cloudRateLimitStatus.useQuota(1);

        if (isValidPageNumber(fullTagList.size(), pageNumber, resourcesPerPage)) {
            fillInPageNumbers(pageNumber, resourcesPerPage, fullTagList, contentResponse);
        }
        return contentResponse;
    }

    /**
     * Gets the rate limit allocated, quota remaining, and the time the given quota is reset
     * <p>
     * This does NOT cost any API usage
     *
     * @return
     */
    @Override
    public CloudResponse getRateLimitStatus(String previousETag) {
        logger.debug("getRateLimitStatus called with: prevETag {}", previousETag);
        CloudResponse cloudResponse = new CloudResponse(HttpURLConnection.HTTP_OK, getHeaders(cloudRateLimitStatus), getHeaders(cloudRateLimitStatus));
        String eTag = getResponseETag(cloudResponse);
        if (eTag.equals(previousETag)) return getNotModifiedResponse();
        return cloudResponse;
    }

    /**
     * Updates the details of the person with details of the updatedPerson if quota is available
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param personId
     * @param updatedPerson
     * @return
     */
    @Override
    public CloudResponse updatePerson(String addressBookName, int personId,
                                      CloudPerson updatedPerson, String previousETag) {

        logger.debug("updatePerson called with: addressbook {}, personid {}, person {}, prevETag {}", addressBookName, personId, updatedPerson, previousETag);
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            CloudPerson resultingPerson = updatePersonDetails(fileData.getAllPersons(), fileData.getAllTags(), personId,
                                                              updatedPerson);
            fileHandler.writeCloudAddressBookToFile(fileData);

            modifyCloudPersonBasedOnChance(resultingPerson);

            CloudResponse cloudResponse = new CloudResponse(HttpURLConnection.HTTP_OK, resultingPerson, getHeaders(cloudRateLimitStatus));
            String eTag = getResponseETag(cloudResponse);
            if (eTag.equals(previousETag)) return getNotModifiedResponse();

            cloudRateLimitStatus.useQuota(1);
            return cloudResponse;
        } catch (NoSuchElementException e) {
            cloudRateLimitStatus.useQuota(1);
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Deletes the person uniquely identified by addressBookName, firstName and lastName, if quota is available
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param personId
     * @return
     */
    @Override
    public CloudResponse deletePerson(String addressBookName, int personId) {
        logger.debug("deletePerson called with: addressbook {}, personid {}", addressBookName, personId);
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            deletePersonFromData(fileData.getAllPersons(), personId);
            fileHandler.writeCloudAddressBookToFile(fileData);

            cloudRateLimitStatus.useQuota(1);
            return getEmptyResponse(HttpURLConnection.HTTP_NO_CONTENT);
        } catch (NoSuchElementException e) {
            cloudRateLimitStatus.useQuota(1);
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Creates a new tag, if quota is available
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param newTag          tag name should not already be used
     * @return
     */
    @Override
    public CloudResponse createTag(String addressBookName, CloudTag newTag, String previousETag) {
        logger.debug("createTag called with: addressbook {}, tag {}, prevETag {}", addressBookName, newTag, previousETag);
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            CloudTag returnedTag = addTag(fileData.getAllTags(), newTag);
            fileHandler.writeCloudAddressBookToFile(fileData);

            modifyCloudTagBasedOnChance(returnedTag);

            CloudResponse cloudResponse = new CloudResponse(HttpURLConnection.HTTP_CREATED, returnedTag, getHeaders(cloudRateLimitStatus));
            String eTag = getResponseETag(cloudResponse);
            if (eTag.equals(previousETag)) return getNotModifiedResponse();

            cloudRateLimitStatus.useQuota(1);
            return cloudResponse;
        } catch (IllegalArgumentException e) {
            cloudRateLimitStatus.useQuota(1);
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Updates details of a tag to details of updatedTag, if quota is available
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param oldTagName        should match a existing tag's name
     * @param updatedTag
     * @return
     */
    @Override
    public CloudResponse editTag(String addressBookName, String oldTagName, CloudTag updatedTag, String previousETag) {
        logger.debug("editTag called with: addressbook {}, tagname {}, tag {}, prevETag {}", addressBookName, oldTagName, updatedTag, previousETag);
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            CloudTag returnedTag = updateTagDetails(fileData.getAllPersons(), fileData.getAllTags(), oldTagName, updatedTag);
            fileHandler.writeCloudAddressBookToFile(fileData);

            modifyCloudTagBasedOnChance(returnedTag);

            CloudResponse cloudResponse = new CloudResponse(HttpURLConnection.HTTP_OK, returnedTag, getHeaders(cloudRateLimitStatus));
            String eTag = getResponseETag(cloudResponse);
            if (eTag.equals(previousETag)) return getNotModifiedResponse();

            cloudRateLimitStatus.useQuota(1);
            return cloudResponse;
        } catch (NoSuchElementException e) {
            cloudRateLimitStatus.useQuota(1);
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Deletes a tag uniquely identified by its name, if quota is available
     * Does not return an eTag
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param tagName           should match an existing tag's name
     * @return
     */
    @Override
    public CloudResponse deleteTag(String addressBookName, String tagName) {
        logger.debug("deleteTag called with: addressbook {}, tagname {}", addressBookName, tagName);
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            deleteTagFromData(fileData.getAllPersons(), fileData.getAllTags(), tagName);
            fileHandler.writeCloudAddressBookToFile(fileData);

            cloudRateLimitStatus.useQuota(1);
            return getEmptyResponse(HttpURLConnection.HTTP_NO_CONTENT);
        } catch (NoSuchElementException e) {
            cloudRateLimitStatus.useQuota(1);
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Creates a new, empty addressbook named addressBookName, if quota is available
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @return
     */
    @Override
    public CloudResponse createAddressBook(String addressBookName) {
        logger.debug("createAddressBook called with: addressbook {}", addressBookName);
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);

        try {
            fileHandler.createCloudAddressBookFile(addressBookName);

            cloudRateLimitStatus.useQuota(1);
            //TODO: Return a wrapped simplified version of an empty addressbook (e.g. only important fields such as name)
            return getEmptyResponse(HttpURLConnection.HTTP_CREATED);
        } catch (DataConversionException | IOException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (IllegalArgumentException e) {
            cloudRateLimitStatus.useQuota(1);
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    /**
     * Gets the list of persons that have been updated after a certain time, if quota is available
     * <p>
     * Consumes 1 + floor(updated person list/resourcesPerPage) API usage
     *
     * @param addressBookName
     * @param timeString
     * @return
     */
    @Override
    public CloudResponse getUpdatedPersons(String addressBookName, String timeString, int pageNumber, int resourcesPerPage, String previousETag) {
        logger.debug("getUpdatedPersons called with: addressbook {}, time {}, pageno {}, resourcesperpage {}, prevETag {}", addressBookName, timeString, pageNumber, resourcesPerPage, previousETag);
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        List<CloudPerson> fullPersonList = new ArrayList<>();
        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            fullPersonList.addAll(fileData.getAllPersons());
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);

        LocalDateTime time = LocalDateTime.parse(timeString);
        List<CloudPerson> filteredList = filterPersonsByTime(fullPersonList, time);

        List<CloudPerson> queryResults = getQueryResults(pageNumber, resourcesPerPage, filteredList);

        mutateCloudPersonList(queryResults);

        CloudResponse contentResponse = new CloudResponse(HttpURLConnection.HTTP_OK, queryResults, getHeaders(cloudRateLimitStatus));
        String eTag = getResponseETag(contentResponse);
        if (eTag.equals(previousETag)) return getNotModifiedResponse();

        cloudRateLimitStatus.useQuota(1);

        if (isValidPageNumber(filteredList.size(), pageNumber, resourcesPerPage)) {
            fillInPageNumbers(pageNumber, resourcesPerPage, filteredList, contentResponse);
        }
        return contentResponse;
    }

    private String getResponseETag(CloudResponse response) {
        return response.getHeaders().get("ETag");
    }

    private HashMap<String, String> getHeaders(CloudRateLimitStatus cloudRateLimitStatus) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("X-RateLimit-Limit", String.valueOf(cloudRateLimitStatus.getQuotaLimit()));
        headers.put("X-RateLimit-Remaining", String.valueOf(cloudRateLimitStatus.getQuotaRemaining()));
        headers.put("X-RateLimit-Reset", String.valueOf(cloudRateLimitStatus.getQuotaReset()));
        return headers;
    }

    /**
     * Fills in the page index details for a cloud response
     *
     * If pageNumber is non-positive, results for pageNumber = 1 will be returned.
     *
     * Previous page and next page will be -1 if pageNumber is not between the first
     * page and last page.
     *
     * @param pageNumber
     * @param resourcesPerPage
     * @param fullResourceList
     * @param contentResponse
     * @param <V>
     */
    private <V> void fillInPageNumbers(int pageNumber, int resourcesPerPage, List<V> fullResourceList, CloudResponse contentResponse) {
        pageNumber = pageNumber < 1 ? 1 : pageNumber;
        int firstPageNumber = 1;
        int lastPageNumber = getLastPageNumber(fullResourceList.size(), resourcesPerPage);
        contentResponse.setFirstPageNo(firstPageNumber);
        contentResponse.setLastPageNo(lastPageNumber);
        if (pageNumber > firstPageNumber) {
            contentResponse.setPreviousPageNo(pageNumber - 1);
        }

        if (pageNumber < lastPageNumber) {
            contentResponse.setNextPageNo(pageNumber + 1);
        }
    }

    private <V> List<V> getQueryResults(int pageNumber, int resourcesPerPage, List<V> fullResourceList) {
        int startIndex = (pageNumber - 1) * resourcesPerPage;
        int endIndex = pageNumber * resourcesPerPage;
        if (endIndex > fullResourceList.size()) {
            endIndex = fullResourceList.size();
        }
        return fullResourceList.subList(startIndex, endIndex);
    }

    private CloudResponse getNotModifiedResponse() {
        logger.debug("Preparing not-modified response.");
        return new CloudResponse(HttpURLConnection.HTTP_NOT_MODIFIED, null, getHeaders(cloudRateLimitStatus));
    }

    private CloudResponse getEmptyResponse(int responseCode) {
        logger.debug("Preparing empty response.");
        return new CloudResponse(responseCode, null, getHeaders(cloudRateLimitStatus));
    }

    private List<CloudPerson> filterPersonsByTime(List<CloudPerson> personList, LocalDateTime time) {
        return personList.stream()
                .filter(person -> !person.getLastUpdatedAt().isBefore(time))
                .collect(Collectors.toList());
    }

    private boolean shouldSimulateNetworkFailure() {
        return shouldSimulateUnreliableNetwork && RANDOM_GENERATOR.nextDouble() <= FAILURE_PROBABILITY;
    }

    private boolean shouldSimulateSlowResponse() {
        return shouldSimulateUnreliableNetwork && RANDOM_GENERATOR.nextDouble() <= NETWORK_DELAY_PROBABILITY;
    }

    private CloudResponse getNetworkFailedResponse() {
        logger.info("Simulated network failure occurred!");
        return new CloudResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    private boolean hasApiQuotaRemaining() {
        logger.info("Current quota left: {}", cloudRateLimitStatus.getQuotaRemaining());
        return cloudRateLimitStatus.getQuotaRemaining() > 0;
    }

    private boolean isExistingPerson(List<CloudPerson> personList, CloudPerson targetPerson) {
        return personList.stream()
                .filter(person -> person.getFirstName().equals(targetPerson.getFirstName())
                        && person.getLastName().equals(targetPerson.getLastName()))
                .findAny()
                .isPresent();
    }

    private boolean isExistingTag(List<CloudTag> tagList, CloudTag targetTag) {
        return tagList.stream()
                .filter(tag -> tag.getName().equals(targetTag.getName()))
                .findAny()
                .isPresent();
    }

    /**
     * Verifies whether newPerson can be added, and adds it to the persons list
     *
     * @param personList
     * @param newPerson
     * @return newPerson, if added successfully
     */
    private CloudPerson addPerson(List<CloudPerson> personList, CloudPerson newPerson)
            throws IllegalArgumentException {
        if (newPerson == null) throw new IllegalArgumentException("Person cannot be null");
        if (!newPerson.isValid()) {
            throw new IllegalArgumentException("Fields cannot be null");
        }
        if (isExistingPerson(personList, newPerson)) throw new IllegalArgumentException("Person already exists");

        CloudPerson personToAdd = generateIdForPerson(personList, newPerson);
        personList.add(personToAdd);

        return personToAdd;
    }

    private CloudPerson generateIdForPerson(List<CloudPerson> personList, CloudPerson newPerson) {
        newPerson.setId(personList.size());
        return newPerson;
    }

    private Optional<CloudPerson> getPerson(List<CloudPerson> personList, int personId) {
        return personList.stream()
                .filter(person -> person.getId() == personId)
                .findAny();
    }

    private CloudPerson updatePersonDetails(List<CloudPerson> personList, List<CloudTag> tagList, int personId,
                                             CloudPerson updatedPerson)
            throws NoSuchElementException {
        CloudPerson oldPerson = getPersonIfExists(personList, personId);
        oldPerson.updatedBy(updatedPerson);

        List<CloudTag> newTags = updatedPerson.getTags().stream()
                                    .filter(tag -> !tagList.contains(tag))
                                    .collect(Collectors.toCollection(ArrayList::new));
        tagList.addAll(newTags);

        return oldPerson;
    }

    private CloudPerson getPersonIfExists(List<CloudPerson> personList, int personId) {
        Optional<CloudPerson> personQueryResult = getPerson(personList, personId);
        if (!personQueryResult.isPresent()) throw new NoSuchElementException("No such person found.");

        return personQueryResult.get();
    }

    private List<CloudPerson> mutateCloudPersonList(List<CloudPerson> CloudPersonList) {
        modifyCloudPersonList(CloudPersonList);
        addCloudPersonsBasedOnChance(CloudPersonList);
        return CloudPersonList;
    }

    private List<CloudTag> mutateCloudTagList(List<CloudTag> CloudTagList) {
        modifyCloudTagListBasedOnChance(CloudTagList);
        addCloudTagsBasedOnChance(CloudTagList);
        return CloudTagList;
    }

    private void modifyCloudPersonList(List<CloudPerson> CloudPersonList) {
        CloudPersonList.stream()
                .forEach(this::modifyCloudPersonBasedOnChance);
    }

    private void modifyCloudTagListBasedOnChance(List<CloudTag> CloudTagList) {
        CloudTagList.stream()
                .forEach(this::modifyCloudTagBasedOnChance);
    }

    private void addCloudPersonsBasedOnChance(List<CloudPerson> personList) {
        for (int i = 0; i < MAX_NUM_PERSONS_TO_ADD; i++) {
            if (shouldSimulateUnreliableNetwork && RANDOM_GENERATOR.nextDouble() <= ADD_PERSON_PROBABILITY) {
                CloudPerson person = new CloudPerson(java.util.UUID.randomUUID().toString(),
                                                     java.util.UUID.randomUUID().toString());
                logger.info("Cloud simulator: adding '{}'", person);
                personList.add(person);
            }
        }
    }

    private void addCloudTagsBasedOnChance(List<CloudTag> tagList) {
        for (int i = 0; i < MAX_NUM_PERSONS_TO_ADD; i++) {
            if (shouldSimulateUnreliableNetwork && RANDOM_GENERATOR.nextDouble() <= ADD_TAG_PROBABILITY) {
                CloudTag tag = new CloudTag(java.util.UUID.randomUUID().toString());
                logger.debug("Cloud simulator: adding tag '{}'", tag);
                tagList.add(tag);
            }
        }
    }

    private void modifyCloudPersonBasedOnChance(CloudPerson CloudPerson) {
        if (!shouldSimulateUnreliableNetwork || RANDOM_GENERATOR.nextDouble() > MODIFY_PERSON_PROBABILITY) return;
        logger.debug("Cloud simulator: modifying person '{}'", CloudPerson);
        CloudPerson.setCity(java.util.UUID.randomUUID().toString());
        CloudPerson.setStreet(java.util.UUID.randomUUID().toString());
        CloudPerson.setPostalCode(String.valueOf(RANDOM_GENERATOR.nextInt(999999)));
    }

    private void modifyCloudTagBasedOnChance(CloudTag CloudTag) {
        if (!shouldSimulateUnreliableNetwork || RANDOM_GENERATOR.nextDouble() > MODIFY_TAG_PROBABILITY) return;
        logger.debug("Cloud simulator: modifying tag '{}'", CloudTag);
        CloudTag.setName(UUID.randomUUID().toString());
    }

    private void delayRandomAmount() {
        long delayAmount = RANDOM_GENERATOR.nextInt(DELAY_RANGE) + MIN_DELAY_IN_SEC;
        try {
            TimeUnit.SECONDS.sleep(delayAmount);
        } catch (InterruptedException e) {
            logger.warn("Error occurred while delaying cloud response.");
        }
    }

    private boolean isValidPageNumber(int dataSize, int pageNumber, int resourcesPerPage) {
        return pageNumber == 1 || getLastPageNumber(dataSize, resourcesPerPage) >= pageNumber;
    }

    private int getLastPageNumber(int dataSize, int resourcesPerPage) {
        return (int) Math.ceil(dataSize/resourcesPerPage);
    }

    private void deletePersonFromData(List<CloudPerson> personList, int personId)
            throws NoSuchElementException {
        CloudPerson deletedPerson = getPersonIfExists(personList, personId);
        deletedPerson.setDeleted(true);
    }

    private CloudTag addTag(List<CloudTag> tagList, CloudTag newTag) {
        if (newTag == null) throw new IllegalArgumentException("Tag cannot be null");
        if (!newTag.isValid()) throw new IllegalArgumentException("Fields cannot be null");
        if (isExistingTag(tagList, newTag)) throw new IllegalArgumentException("Tag already exists");
        tagList.add(newTag);
        return newTag;
    }

    private Optional<CloudTag> getTag(List<CloudTag> tagList, String tagName) {
        return tagList.stream()
                .filter(tag -> tag.getName().equals(tagName))
                .findAny();
    }

    private CloudTag getTagIfExists(List<CloudTag> tagList, String tagName) {
        Optional<CloudTag> tagQueryResult = getTag(tagList, tagName);
        if (!tagQueryResult.isPresent()) throw new NoSuchElementException("No such tag found.");

        return tagQueryResult.get();
    }

    private CloudTag updateTagDetails(List<CloudPerson> personList, List<CloudTag> tagList, String oldTagName, CloudTag updatedTag)
            throws NoSuchElementException {
        CloudTag oldTag = getTagIfExists(tagList, oldTagName);
        oldTag.updatedBy(updatedTag);
        personList.stream()
                .forEach(person -> {
                    List<CloudTag> personTags = person.getTags();
                    personTags.stream()
                            .filter(personTag -> personTag.getName().equals(oldTagName))
                            .forEach(personTag -> personTag.updatedBy(updatedTag));
                });
        return oldTag;
    }

    private void deleteTagFromData(List<CloudPerson> personList, List<CloudTag> tagList, String tagName) throws NoSuchElementException {
        CloudTag tag = getTagIfExists(tagList, tagName);
        // This may differ from how GitHub does it, but we won't know for sure
        tagList.remove(tag);
        personList.stream()
                .forEach(person -> {
                    List<CloudTag> personTags = person.getTags();
                    personTags = personTags.stream()
                            .filter(personTag -> !personTag.getName().equals(tagName))
                            .collect(Collectors.toList());
                    person.setTags(personTags);
                });
    }
}
