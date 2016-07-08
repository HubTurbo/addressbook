package address.sync.cloud;

import address.sync.cloud.model.CloudAddressBook;
import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;
import address.exceptions.DataConversionException;
import address.sync.cloud.request.CreatePersonRequest;
import address.sync.cloud.request.CreateTagRequest;
import address.sync.cloud.request.DeletePersonRequest;
import address.sync.cloud.request.DeleteTagRequest;
import address.util.AppLogger;
import address.util.Config;
import address.util.LoggerManager;

import java.io.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

/**
 * This class is responsible for emulating a cloud with an API similar
 * to a subset of GitHub's, together with a given hourly API quota.
 *
 * Requests for a full list of objects should be done in pages. Responses
 * will include first page/prev page/next page/last page if they exist.
 *
 * Providing previous request's eTag may return a NOT_MODIFIED response if the response's eTag has not changed.
 * All requests (including bad ones) will consume API, unless it is a response with NOT_MODIFIED.
 */
public class CloudSimulator implements IRemote {
    private static final AppLogger logger = LoggerManager.getLogger(CloudSimulator.class);
    private static final int API_QUOTA_PER_HOUR = 5000;

    protected CloudRateLimitStatus cloudRateLimitStatus;
    protected CloudRequestQueue cloudRequestQueue;
    protected CloudFileHandler cloudFileHandler;

    protected CloudSimulator(CloudFileHandler cloudFileHandler, CloudRateLimitStatus cloudRateLimitStatus) {
        this.cloudFileHandler = cloudFileHandler;
        this.cloudRateLimitStatus = cloudRateLimitStatus;// TODO: ADD CLOUD REQUEST QUEUE STUB DURING TESTS
    }

    public CloudSimulator(Config config) {
        cloudFileHandler = new CloudFileHandler(); // to remove after requests have been migrated
        cloudRateLimitStatus = new CloudRateLimitStatus(API_QUOTA_PER_HOUR);
        cloudRateLimitStatus.restartQuotaTimer();
        cloudRequestQueue = new CloudRequestQueue(cloudFileHandler, cloudRateLimitStatus);
        try {
            cloudFileHandler.createAddressBookIfAbsent(config.getAddressBookName());
        } catch (IOException | DataConversionException e) {
            logger.fatal("Error initializing cloud file for '{}'", config.getAddressBookName());
            assert false : "Error initializing cloud file: " + config.getAddressBookName();
        }
    }

    /**
     * Attempts to create a person if quota is available
     *
     * A new ID for the new person will be generated, and the ID field in the given newPerson will be ignored
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param newPerson
     * @param previousETag
     * @return a response wrapper, containing the added person if successful
     */
    @Override
    public RemoteResponse createPerson(String addressBookName, CloudPerson newPerson, String previousETag) {
        logger.debug("createPerson called with: addressbook {}, person {}, prevETag {}", addressBookName, newPerson,
                previousETag);
        if (!hasApiQuotaRemaining()) return RemoteResponse.getForbiddenResponse(cloudRateLimitStatus);

        try {
            CompletableFuture<CloudPerson> resultContainer = new CompletableFuture<>();
            CreatePersonRequest createPersonRequest = new CreatePersonRequest(addressBookName, newPerson, resultContainer);
            cloudRequestQueue.submitRequest(createPersonRequest);

            CloudPerson returnedPerson = resultContainer.get();
            return new RemoteResponse(HttpURLConnection.HTTP_CREATED, returnedPerson, cloudRateLimitStatus,
                                      previousETag);
        } catch (InterruptedException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            return getResponseForCause(cause);
        }
    }

    /**
     * Returns a response wrapper containing the list of persons if quota is available
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param pageNumber page to obtain list from
     * @param resourcesPerPage
     * @param previousETag etag of previous response, if any
     * @return
     */
    @Override
    public RemoteResponse getPersons(String addressBookName, int pageNumber, int resourcesPerPage,
                                     String previousETag) {
        logger.debug("getPersons called with: addressbook {}, page {}, resourcesperpage {}, prevETag {}",
                addressBookName, pageNumber, resourcesPerPage, previousETag);
        if (!hasApiQuotaRemaining()) return RemoteResponse.getForbiddenResponse(cloudRateLimitStatus);

        List<CloudPerson> fullPersonList = new ArrayList<>();
        try {
            CloudAddressBook fileData = cloudFileHandler.readCloudAddressBook(addressBookName);
            fullPersonList.addAll(fileData.getAllPersons());
        } catch (FileNotFoundException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_NOT_FOUND);
        } catch (DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        List<CloudPerson> queryResults = getQueryResults(pageNumber, resourcesPerPage, fullPersonList);
        RemoteResponse contentResponse = new RemoteResponse(HttpURLConnection.HTTP_OK, queryResults,
                                                            cloudRateLimitStatus, previousETag);

        if (isNotModifiedResponse(contentResponse)) return contentResponse;

        if (isValidPageNumber(fullPersonList.size(), pageNumber, resourcesPerPage)) {
            fillInPageNumbers(pageNumber, resourcesPerPage, fullPersonList, contentResponse);
        }
        return contentResponse;
    }

    private boolean isNotModifiedResponse(RemoteResponse contentResponse) {
        return contentResponse.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED;
    }

    /**
     * Returns a response wrapper containing the list of tags if quota is available
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param pageNumber
     * @param resourcesPerPage
     * @param previousETag
     * @return
     */
    @Override
    public RemoteResponse getTags(String addressBookName, int pageNumber, int resourcesPerPage, String previousETag) {
        logger.debug("getTags called with: addressbook {}, page {}, resourcesperpage {}, prevETag {}", addressBookName,
                pageNumber, resourcesPerPage, previousETag);
        if (!hasApiQuotaRemaining()) return RemoteResponse.getForbiddenResponse(cloudRateLimitStatus);

        List<CloudTag> fullTagList = new ArrayList<>();

        try {
            CloudAddressBook fileData = cloudFileHandler.readCloudAddressBook(addressBookName);
            fullTagList.addAll(fileData.getAllTags());
        } catch (FileNotFoundException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_NOT_FOUND);
        } catch (DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        List<CloudTag> queryResults = getQueryResults(pageNumber, resourcesPerPage, fullTagList);
        RemoteResponse contentResponse = new RemoteResponse(HttpURLConnection.HTTP_OK, queryResults,
                                                            cloudRateLimitStatus, previousETag);
        if (isNotModifiedResponse(contentResponse)) return contentResponse;

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
     * @param previousETag
     * @return
     */
    @Override
    public RemoteResponse getRateLimitStatus(String previousETag) {
        // TODO: Figure out GitHub response for limit status if ETag is provided
        logger.debug("getRateLimitStatus called with: prevETag {}", previousETag);
        return RemoteResponse.getLimitStatusResponse(cloudRateLimitStatus);
    }

    /**
     * Updates the details of the person with details of the updatedPerson if quota is available
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param personId
     * @param updatedPerson
     * @param previousETag
     * @return
     */
    @Override
    public RemoteResponse updatePerson(String addressBookName, int personId,
                                       CloudPerson updatedPerson, String previousETag) {

        logger.debug("updatePerson called with: addressbook {}, personid {}, person {}, prevETag {}", addressBookName,
                personId, updatedPerson, previousETag);
        if (!hasApiQuotaRemaining()) return RemoteResponse.getForbiddenResponse(cloudRateLimitStatus);
        try {
            CloudAddressBook fileData = cloudFileHandler.readCloudAddressBook(addressBookName);
            CloudPerson resultingPerson = updatePersonDetails(fileData.getAllPersons(), fileData.getAllTags(), personId,
                                                              updatedPerson);
            cloudFileHandler.writeCloudAddressBook(fileData);
            return new RemoteResponse(HttpURLConnection.HTTP_OK, resultingPerson, cloudRateLimitStatus, previousETag);
        } catch (NoSuchElementException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_NOT_FOUND);
        } catch (DataConversionException e) {
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
    public RemoteResponse deletePerson(String addressBookName, int personId) {
        logger.debug("deletePerson called with: addressbook {}, personid {}", addressBookName, personId);
        try {
            CompletableFuture<Void> resultContainer = new CompletableFuture<>();
            DeletePersonRequest deletePersonRequest = new DeletePersonRequest(addressBookName, personId, resultContainer);
            logger.debug("Submitting delete person request");
            cloudRequestQueue.submitRequest(deletePersonRequest);

            resultContainer.get();
            logger.info("Person #{} successful deleted", personId);
            return getEmptyResponse(HttpURLConnection.HTTP_NO_CONTENT);
        } catch (InterruptedException e) {
            logger.warn("Request thread ended prematurely");
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            return getResponseForCause(cause);
        }
    }

    private RemoteResponse getResponseForCause(Throwable cause) {
        if (cause instanceof NoSuchElementException || cause instanceof  IllegalArgumentException) {
            logger.warn("Bad request found");
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } else if (cause instanceof FileNotFoundException) {
            logger.warn("No such element");
            return getEmptyResponse(HttpURLConnection.HTTP_NOT_FOUND);
        } else if (cause instanceof RejectedExecutionException) {
            logger.warn("No more API left");
            return RemoteResponse.getForbiddenResponse(cloudRateLimitStatus);
        } else {
            logger.warn("Internal error");
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
     * @param previousETag
     * @return
     */
    @Override
    public RemoteResponse createTag(String addressBookName, CloudTag newTag, String previousETag) {
        logger.debug("createTag called with: addressbook {}, tag {}, prevETag {}", addressBookName, newTag,
                previousETag);
        try {
            CompletableFuture<CloudTag> resultContainer = new CompletableFuture<>();
            CreateTagRequest createTagRequest = new CreateTagRequest(addressBookName, newTag, resultContainer);
            cloudRequestQueue.submitRequest(createTagRequest);

            CloudTag returnedTag = resultContainer.get();

            return new RemoteResponse(HttpURLConnection.HTTP_CREATED, returnedTag, cloudRateLimitStatus, previousETag);
        } catch (InterruptedException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            return getResponseForCause(cause);
        }
    }

    /**
     * Updates details of a tag to details of updatedTag, if quota is available
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param oldTagName        should match an existing tag's name
     * @param updatedTag
     * @param previousETag
     * @return
     */
    @Override
    public RemoteResponse editTag(String addressBookName, String oldTagName, CloudTag updatedTag, String previousETag) {
        logger.debug("editTag called with: addressbook {}, tagname {}, tag {}, prevETag {}", addressBookName,
                oldTagName, updatedTag, previousETag);
        if (!hasApiQuotaRemaining()) return RemoteResponse.getForbiddenResponse(cloudRateLimitStatus);
        try {
            CloudAddressBook fileData = cloudFileHandler.readCloudAddressBook(addressBookName);
            CloudTag returnedTag = updateTagDetails(fileData.getAllPersons(), fileData.getAllTags(), oldTagName,
                                                    updatedTag);
            cloudFileHandler.writeCloudAddressBook(fileData);
            return new RemoteResponse(HttpURLConnection.HTTP_OK, returnedTag, cloudRateLimitStatus, previousETag);
        } catch (NoSuchElementException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (FileNotFoundException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_NOT_FOUND);
        } catch (DataConversionException e) {
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
    public RemoteResponse deleteTag(String addressBookName, String tagName) {
        logger.debug("deleteTag called with: addressbook {}, tagname {}", addressBookName, tagName);
        try {
            CompletableFuture<Void> resultContainer = new CompletableFuture<>();
            DeleteTagRequest deleteTagRequest = new DeleteTagRequest(addressBookName, tagName, resultContainer);
            cloudRequestQueue.submitRequest(deleteTagRequest);

            resultContainer.get();
            return getEmptyResponse(HttpURLConnection.HTTP_NO_CONTENT);
        } catch (InterruptedException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            return getResponseForCause(cause);
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
    public RemoteResponse createAddressBook(String addressBookName) {
        logger.debug("createAddressBook called with: addressbook {}", addressBookName);
        if (!hasApiQuotaRemaining()) return RemoteResponse.getForbiddenResponse(cloudRateLimitStatus);

        try {
            cloudFileHandler.createAddressBook(addressBookName);

            //TODO: Return a wrapped simplified version of an empty addressbook (e.g. only fields such as name)
            return getEmptyResponse(HttpURLConnection.HTTP_CREATED);
        } catch (DataConversionException | IOException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        } catch (IllegalArgumentException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    /**
     * Gets the list of persons that have been updated after a certain time, if quota is available
     * <p>
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param timeString
     * @param pageNumber
     * @param resourcesPerPage
     * @param previousETag
     * @return
     */
    @Override
    public RemoteResponse getUpdatedPersons(String addressBookName, String timeString, int pageNumber,
                                            int resourcesPerPage, String previousETag) {
        logger.debug("getUpdatedPersons called with: addressbook {}, time {}, pageno {}, resourcesperpage {}, prevETag {}",
                addressBookName, timeString, pageNumber, resourcesPerPage, previousETag);

        if (!hasApiQuotaRemaining()) return RemoteResponse.getForbiddenResponse(cloudRateLimitStatus);
        List<CloudPerson> fullPersonList = new ArrayList<>();
        try {
            CloudAddressBook fileData = cloudFileHandler.readCloudAddressBook(addressBookName);
            fullPersonList.addAll(fileData.getAllPersons());
        } catch (FileNotFoundException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_NOT_FOUND);
        } catch (DataConversionException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        LocalDateTime time = LocalDateTime.parse(timeString);
        List<CloudPerson> filteredList = filterPersonsByTime(fullPersonList, time);

        List<CloudPerson> queryResults = getQueryResults(pageNumber, resourcesPerPage, filteredList);

        RemoteResponse contentResponse = new RemoteResponse(HttpURLConnection.HTTP_OK, queryResults,
                                                            cloudRateLimitStatus, previousETag);
        if (isNotModifiedResponse(contentResponse)) return contentResponse;

        if (isValidPageNumber(filteredList.size(), pageNumber, resourcesPerPage)) {
            fillInPageNumbers(pageNumber, resourcesPerPage, filteredList, contentResponse);
        }
        return contentResponse;
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
    private <V> void fillInPageNumbers(int pageNumber, int resourcesPerPage, List<V> fullResourceList,
                                       RemoteResponse contentResponse) {
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

    private RemoteResponse getEmptyResponse(int responseCode) {
        logger.debug("Preparing empty response: {}", responseCode);
        return new RemoteResponse(responseCode, null, cloudRateLimitStatus, null);
    }

    private List<CloudPerson> filterPersonsByTime(List<CloudPerson> personList, LocalDateTime time) {
        return personList.stream()
                .filter(person -> !person.getLastUpdatedAt().isBefore(time))
                .collect(Collectors.toList());
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

    private Optional<CloudPerson> getPerson(List<CloudPerson> personList, int personId) {
        return personList.stream()
                .filter(person -> person.getId() == personId)
                .findAny();
    }

    private CloudPerson updatePersonDetails(List<CloudPerson> personList, List<CloudTag> tagList, int personId,
                                             CloudPerson updatedPerson) throws NoSuchElementException {
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

    private boolean isValidPageNumber(int dataSize, int pageNumber, int resourcesPerPage) {
        return pageNumber == 1 || getLastPageNumber(dataSize, resourcesPerPage) >= pageNumber;
    }

    private int getLastPageNumber(int dataSize, int resourcesPerPage) {
        return (int) Math.ceil(dataSize/resourcesPerPage);
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

    private CloudTag updateTagDetails(List<CloudPerson> personList, List<CloudTag> tagList, String oldTagName,
                                      CloudTag updatedTag) throws NoSuchElementException {
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

}
