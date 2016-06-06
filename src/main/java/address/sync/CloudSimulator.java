package address.sync;

import address.sync.model.CloudAddressBook;
import address.sync.model.CloudTag;
import address.sync.model.CloudPerson;
import address.util.JsonUtil;
import address.util.TickingTimer;
import com.fasterxml.jackson.core.JsonProcessingException;
import address.exceptions.DataConversionException;
import java.io.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CloudSimulator implements ICloudSimulator {
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
    private RateLimitStatus rateLimitStatus;
    private boolean shouldSimulateUnreliableNetwork;
    private TickingTimer timer;
    private CloudFileHandler fileHandler;

    CloudSimulator(CloudFileHandler fileHandler, RateLimitStatus rateLimitStatus,
                   boolean shouldSimulateUnreliableNetwork) {
        this.fileHandler = fileHandler;
        this.rateLimitStatus = rateLimitStatus;
        this.shouldSimulateUnreliableNetwork = shouldSimulateUnreliableNetwork;
        resetQuotaAndRestartTimer();
    }

    CloudSimulator(boolean shouldSimulateUnreliableNetwork) {
        fileHandler = new CloudFileHandler();
        rateLimitStatus = new RateLimitStatus(API_QUOTA_PER_HOUR, API_QUOTA_PER_HOUR, getNextResetTime());
        this.shouldSimulateUnreliableNetwork = shouldSimulateUnreliableNetwork;
        resetQuotaAndRestartTimer();
    }

    public static ByteArrayInputStream convertToInputStream(Object object) {
        try {
            return new ByteArrayInputStream(JsonUtil.toJsonString(object).getBytes());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Attempts to create a person if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param newPerson
     * @return a response wrapper, containing the added person if successful
     */
    @Override
    public RawCloudResponse createPerson(String addressBookName, CloudPerson newPerson, String previousETag) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);

        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            CloudPerson returnedPerson = addPerson(fileData.getAllPersons(), newPerson);
            fileHandler.writeCloudAddressBookToFile(fileData);

            modifyCloudPersonBasedOnChance(returnedPerson);
            InputStream bodyStream = convertToInputStream(returnedPerson);
            String eTag = RawCloudResponse.getETag(bodyStream, false);

            if (eTag.equals(previousETag)) return getNotModifiedResponse(eTag);

            rateLimitStatus.useQuota(1);
            return new RawCloudResponse(HttpURLConnection.HTTP_OK, bodyStream, getHeaders(eTag, rateLimitStatus));
        } catch (IllegalArgumentException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Returns a response wrapper containing the list of persons if quota is available
     *
     * Consumes 1 + floor(persons size/resourcesPerPage) API usage
     *
     * @param addressBookName
     * @param resourcesPerPage
     * @return
     */
    @Override
    public RawCloudResponse getPersons(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
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
        InputStream bodyStream = convertToInputStream(queryResults);
        String eTag = RawCloudResponse.getETag(bodyStream, false);
        if (eTag.equals(previousETag)) return getNotModifiedResponse(eTag);

        rateLimitStatus.useQuota(1);

        RawCloudResponse contentResponse = new RawCloudResponse(HttpURLConnection.HTTP_OK, bodyStream,
                                                                getHeaders(eTag, rateLimitStatus));
        if (isValidPageNumber(fullPersonList.size(), pageNumber, resourcesPerPage)) {
            fillInPageNumbers(pageNumber, resourcesPerPage, fullPersonList, contentResponse);
        }
        return contentResponse;
    }

    /**
     * Returns a response wrapper containing the list of tags if quota is available
     *
     * Consumes 1 + floor(tag list/resourcesPerPage) API usage
     *
     * @param addressBookName
     * @param resourcesPerPage
     * @return
     */
    @Override
    public RawCloudResponse getTags(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
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
        InputStream bodyStream = convertToInputStream(queryResults);
        String eTag = RawCloudResponse.getETag(bodyStream, false);
        if (eTag.equals(previousETag)) return getNotModifiedResponse(eTag);

        rateLimitStatus.useQuota(1);

        RawCloudResponse contentResponse = new RawCloudResponse(HttpURLConnection.HTTP_OK, bodyStream, getHeaders(eTag, rateLimitStatus));
        if (isValidPageNumber(fullTagList.size(), pageNumber, resourcesPerPage)) {
            fillInPageNumbers(pageNumber, resourcesPerPage, fullTagList, contentResponse);
        }
        return contentResponse;
    }

    /**
     * Gets the rate limit allocated, quota remaining, and the time the given quota is reset
     *
     * This does NOT cost any API usage
     *
     * @return
     */
    @Override
    public RawCloudResponse getRateLimitStatus(String previousETag) {
        InputStream bodyStream = convertToInputStream(rateLimitStatus);
        String eTag = RawCloudResponse.getETag(bodyStream, false);
        if (eTag.equals(previousETag)) return getNotModifiedResponse(eTag);
        return new RawCloudResponse(HttpURLConnection.HTTP_OK, bodyStream, getHeaders(eTag, rateLimitStatus));
    }

    /**
     * Updates the details of the person with details of the updatedPerson if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param oldFirstName
     * @param oldLastName
     * @param updatedPerson
     * @return
     */
    @Override
    public RawCloudResponse updatePerson(String addressBookName, String oldFirstName, String oldLastName,
                                         CloudPerson updatedPerson, String previousETag) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            CloudPerson resultingPerson = updatePersonDetails(fileData.getAllPersons(), oldFirstName, oldLastName,
                                                              updatedPerson);
            fileHandler.writeCloudAddressBookToFile(fileData);

            modifyCloudPersonBasedOnChance(resultingPerson);

            InputStream bodyStream = convertToInputStream(resultingPerson);
            String eTag = RawCloudResponse.getETag(bodyStream, false);
            if (eTag.equals(previousETag)) return getNotModifiedResponse(eTag);

            rateLimitStatus.useQuota(1);
            return new RawCloudResponse(HttpURLConnection.HTTP_OK, bodyStream, getHeaders(eTag, rateLimitStatus));
        } catch (NoSuchElementException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Deletes the person uniquely identified by addressBookName, firstName and lastName, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param firstName
     * @param lastName
     * @return
     */
    @Override
    public RawCloudResponse deletePerson(String addressBookName, String firstName, String lastName) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            deletePersonFromData(fileData.getAllPersons(), firstName, lastName);
            fileHandler.writeCloudAddressBookToFile(fileData);

            rateLimitStatus.useQuota(1);
            return getEmptyResponse(HttpURLConnection.HTTP_NO_CONTENT);
        } catch (NoSuchElementException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Creates a new tag, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param newTag          tag name should not already be used
     * @return
     */
    @Override
    public RawCloudResponse createTag(String addressBookName, CloudTag newTag, String previousETag) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            CloudTag returnedTag = addTag(fileData.getAllTags(), newTag);
            fileHandler.writeCloudAddressBookToFile(fileData);

            modifyCloudTagBasedOnChance(returnedTag);
            InputStream bodyStream = convertToInputStream(returnedTag);
            String eTag = RawCloudResponse.getETag(bodyStream, false);
            if (eTag.equals(previousETag)) return getNotModifiedResponse(eTag);

            rateLimitStatus.useQuota(1);
            return new RawCloudResponse(HttpURLConnection.HTTP_CREATED, bodyStream, getHeaders(eTag, rateLimitStatus));
        } catch (IllegalArgumentException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Updates details of a tag to details of updatedTag, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param oldTagName
     * @param updatedTag
     * @return
     */
    @Override
    public RawCloudResponse editTag(String addressBookName, String oldTagName, CloudTag updatedTag, String previousETag) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            CloudTag returnedTag = updateTagDetails(fileData.getAllPersons(), fileData.getAllTags(), oldTagName, updatedTag);
            fileHandler.writeCloudAddressBookToFile(fileData);

            modifyCloudTagBasedOnChance(returnedTag);
            InputStream bodyStream = convertToInputStream(returnedTag);
            String eTag = RawCloudResponse.getETag(bodyStream, false);
            if (eTag.equals(previousETag)) return getNotModifiedResponse(eTag);

            rateLimitStatus.useQuota(1);
            return new RawCloudResponse(HttpURLConnection.HTTP_OK, bodyStream, getHeaders(eTag, rateLimitStatus));
        } catch (NoSuchElementException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Deletes a tag uniquely identified by its name, if quota is available
     * Does not return an eTag
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param tagName
     * @return
     */
    @Override
    public RawCloudResponse deleteTag(String addressBookName, String tagName) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        try {
            CloudAddressBook fileData = fileHandler.readCloudAddressBookFromFile(addressBookName);
            deleteTagFromData(fileData.getAllPersons(), fileData.getAllTags(), tagName);
            fileHandler.writeCloudAddressBookToFile(fileData);

            rateLimitStatus.useQuota(1);
            return getEmptyResponse(HttpURLConnection.HTTP_NO_CONTENT);
        } catch (NoSuchElementException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException | DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Creates a new, empty addressbook named addressBookName, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @return
     */
    @Override
    public RawCloudResponse createAddressBook(String addressBookName) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!hasApiQuotaRemaining()) return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);

        try {
            fileHandler.createCloudAddressBookFile(addressBookName);

            rateLimitStatus.useQuota(1);
            //TODO: Wrap a simplified version of an empty addressbook (e.g. only important fields such as name)
            return getEmptyResponse(HttpURLConnection.HTTP_OK);
        } catch (DataConversionException | IOException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (IllegalArgumentException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    /**
     * Gets the list of persons that have been updated after a certain time, if quota is available
     *
     * Consumes 1 + floor(updated person list/resourcesPerPage) API usage
     *
     * @param addressBookName
     * @param timeString
     * @return
     */
    @Override
    public RawCloudResponse getUpdatedPersons(String addressBookName, String timeString,int pageNumber, int resourcesPerPage, String previousETag) {
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
        InputStream bodyStream = convertToInputStream(queryResults);
        String eTag = RawCloudResponse.getETag(bodyStream, false);
        if (eTag.equals(previousETag)) return getNotModifiedResponse(eTag);

        rateLimitStatus.useQuota(1);

        RawCloudResponse contentResponse = new RawCloudResponse(HttpURLConnection.HTTP_OK, bodyStream, getHeaders(eTag, rateLimitStatus));
        if (isValidPageNumber(filteredList.size(), pageNumber, resourcesPerPage)) {
            fillInPageNumbers(pageNumber, resourcesPerPage, filteredList, contentResponse);
        }
        return contentResponse;
    }

    private HashMap<String, String> getHeaders(String eTag, RateLimitStatus rateLimitStatus) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("X-RateLimit-Limit", String.valueOf(rateLimitStatus.getQuotaLimit()));
        headers.put("X-RateLimit-Remaining", String.valueOf(rateLimitStatus.getQuotaRemaining()));
        headers.put("X-RateLimit-Reset", String.valueOf(rateLimitStatus.getQuotaReset()));
        headers.put("ETag", eTag);
        return headers;
    }

    private <V> void fillInPageNumbers(int pageNumber, int resourcesPerPage, List<V> fullResourceList, RawCloudResponse contentResponse) {
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
        int startIndex = (pageNumber - 1) * resourcesPerPage + 1;
        int endIndex = startIndex + resourcesPerPage;
        return getResultList(fullResourceList, startIndex, endIndex);
    }

    private <V> List<V> getResultList(List<V> fullResourceList, int startingIndex, int endingIndex) {
        int totalResources = fullResourceList.size();
        if (startingIndex >= totalResources) return new ArrayList<>();
        if (endingIndex <= 0) return new ArrayList<>();

        if (endingIndex > totalResources) {
            endingIndex = totalResources;
        }

        return fullResourceList.subList(startingIndex, endingIndex);
    }

    private RawCloudResponse getNotModifiedResponse(String eTag) {
        return new RawCloudResponse(HttpURLConnection.HTTP_NOT_MODIFIED, null, getHeaders(eTag, rateLimitStatus));
    }

    private RawCloudResponse getEmptyResponse(int responseCode) {
        return new RawCloudResponse(responseCode, null, getHeaders(null, rateLimitStatus));
    }

    private void resetQuotaAndRestartTimer() {
        long nextResetTime = getNextResetTime();
        rateLimitStatus.setQuotaResetTime(nextResetTime);
        rateLimitStatus.setQuotaRemaining(API_QUOTA_PER_HOUR);
        if (timer != null) {
            timer.stop();
        }
        int timeout = (int) (rateLimitStatus.getQuotaReset() - LocalDateTime.now().toEpochSecond(getSystemTimezone()));
        timer = new TickingTimer("Cloud Quota Reset Time", timeout, this::printTimeLeft,
                                this::resetQuotaAndRestartTimer, TimeUnit.SECONDS);
        timer.start();
    }

    private void printTimeLeft(int timeLeft) {
        if (timeLeft % 60 == 0) System.out.println(timeLeft + " seconds remaining to quota reset.");
    }

    private List<CloudPerson> filterPersonsByTime(List<CloudPerson> personList, LocalDateTime time) {
        return personList.stream()
                .filter(person -> !person.getLastUpdatedAt().isBefore(time))
                .collect(Collectors.toList());
    }

    private long getNextResetTime() {
        LocalDateTime curTime = LocalDateTime.now();
        LocalDateTime nearestHour = LocalDateTime.of(
                curTime.getYear(), curTime.getMonth(), curTime.getDayOfMonth(), curTime.getHour() + 1,
                0, 0, 0);

        return nearestHour.toEpochSecond(getSystemTimezone());
    }

    private ZoneOffset getSystemTimezone() {
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneOffset.systemDefault());
        return zonedDateTime.getOffset();
    }

    private boolean shouldSimulateNetworkFailure() {
        return shouldSimulateUnreliableNetwork && RANDOM_GENERATOR.nextDouble() <= FAILURE_PROBABILITY;
    }

    private boolean shouldSimulateSlowResponse() {
        return shouldSimulateUnreliableNetwork && RANDOM_GENERATOR.nextDouble() <= NETWORK_DELAY_PROBABILITY;
    }

    private RawCloudResponse getNetworkFailedResponse() {
        System.out.println("Cloud simulator: failure occurred! Could not retrieve data");
        return new RawCloudResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    private boolean hasApiQuotaRemaining() {
        System.out.println("Current quota left: " + rateLimitStatus.getQuotaRemaining());
        return rateLimitStatus.getQuotaRemaining() > 0;
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
                .filter(tag -> !targetTag.getName().equals(targetTag.getName()))
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

        personList.add(newPerson);

        return newPerson;
    }

    private Optional<CloudPerson> getPerson(List<CloudPerson> personList, String firstName, String lastName) {
        return personList.stream()
                .filter(person -> person.getFirstName().equals(firstName)
                        && person.getLastName().equals(lastName))
                .findAny();
    }

    private CloudPerson updatePersonDetails(List<CloudPerson> personList, String oldFirstName, String oldLastName,
                                            CloudPerson updatedPerson) throws NoSuchElementException {
        CloudPerson oldPerson = getPersonIfExists(personList, oldFirstName, oldLastName);
        oldPerson.updatedBy(updatedPerson);
        return oldPerson;
    }

    private CloudPerson getPersonIfExists(List<CloudPerson> personList, String oldFirstName, String oldLastName) {
        Optional<CloudPerson> personQueryResult = getPerson(personList, oldFirstName, oldLastName);
        if (!personQueryResult.isPresent()) throw new NoSuchElementException("No such person found.");

        return personQueryResult.get();
    }

    private List<CloudPerson> mutateCloudPersonList(List<CloudPerson> cloudPersonList) {
        modifyCloudPersonList(cloudPersonList);
        addCloudPersonsBasedOnChance(cloudPersonList);
        return cloudPersonList;
    }

    private List<CloudTag> mutateCloudTagList(List<CloudTag> cloudTagList) {
        modifyCloudTagListBasedOnChance(cloudTagList);
        addCloudTagsBasedOnChance(cloudTagList);
        return cloudTagList;
    }

    private void modifyCloudPersonList(List<CloudPerson> cloudPersonList) {
        cloudPersonList.stream()
                .forEach(cloudPerson -> modifyCloudPersonBasedOnChance(cloudPerson));
    }

    private void modifyCloudTagListBasedOnChance(List<CloudTag> cloudTagList) {
        cloudTagList.stream()
                .forEach(cloudTag -> modifyCloudTagBasedOnChance(cloudTag));
    }

    private void addCloudPersonsBasedOnChance(List<CloudPerson> personList) {
        for (int i = 0; i < MAX_NUM_PERSONS_TO_ADD; i++) {
            if (shouldSimulateUnreliableNetwork && RANDOM_GENERATOR.nextDouble() <= ADD_PERSON_PROBABILITY) {
                CloudPerson person = new CloudPerson(java.util.UUID.randomUUID().toString(),
                                                     java.util.UUID.randomUUID().toString());
                System.out.println("Cloud simulator: adding " + person);
                personList.add(person);
            }
        }
    }

    private void addCloudTagsBasedOnChance(List<CloudTag> tagList) {
        for (int i = 0; i < MAX_NUM_PERSONS_TO_ADD; i++) {
            if (shouldSimulateUnreliableNetwork && RANDOM_GENERATOR.nextDouble() <= ADD_TAG_PROBABILITY) {
                CloudTag tag = new CloudTag(java.util.UUID.randomUUID().toString());
                System.out.println("Cloud simulator: adding tag '" + tag + "'");
                tagList.add(tag);
            }
        }
    }

    private void modifyCloudPersonBasedOnChance(CloudPerson cloudPerson) {
        if (!shouldSimulateUnreliableNetwork || RANDOM_GENERATOR.nextDouble() > MODIFY_PERSON_PROBABILITY) return;
        System.out.println("Cloud simulator: modifying person '" + cloudPerson + "'");
        cloudPerson.setCity(java.util.UUID.randomUUID().toString());
        cloudPerson.setStreet(java.util.UUID.randomUUID().toString());
        cloudPerson.setPostalCode(String.valueOf(RANDOM_GENERATOR.nextInt(999999)));
    }

    private void modifyCloudTagBasedOnChance(CloudTag cloudTag) {
        if (!shouldSimulateUnreliableNetwork || RANDOM_GENERATOR.nextDouble() > MODIFY_TAG_PROBABILITY) return;
        System.out.println("Cloud simulator: modifying tag '" + cloudTag + "'");
        cloudTag.setName(UUID.randomUUID().toString());
    }

    private void delayRandomAmount() {
        long delayAmount = RANDOM_GENERATOR.nextInt(DELAY_RANGE) + MIN_DELAY_IN_SEC;
        try {
            TimeUnit.SECONDS.sleep(delayAmount);
        } catch (InterruptedException e) {
            System.out.println("Error occurred while delaying cloud response.");
        }
    }

    private boolean isValidPageNumber(int dataSize, int pageNumber, int resourcesPerPage) {
        return pageNumber == 1 || getLastPageNumber(dataSize, resourcesPerPage) >= pageNumber;
    }

    private int getLastPageNumber(int dataSize, int resourcesPerPage) {
        return (int) Math.ceil(dataSize/resourcesPerPage);
    }

    private void deletePersonFromData(List<CloudPerson> personList, String firstName, String lastName)
            throws NoSuchElementException {
        CloudPerson deletedPerson = getPersonIfExists(personList, firstName, lastName);
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
