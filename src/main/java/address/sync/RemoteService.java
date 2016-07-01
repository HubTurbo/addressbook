package address.sync;

import address.model.datatypes.person.ReadOnlyPerson;
import address.model.datatypes.tag.Tag;
import address.model.datatypes.person.Person;
import address.sync.cloud.CloudSimulator;
import address.sync.cloud.RemoteResponse;
import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;
import address.util.AppLogger;
import address.util.Config;
import address.util.JsonUtil;
import address.util.LoggerManager;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This component is responsible for providing a high-level API for communication with the remote,
 * as well as the reformatting the remote's response into a format understood by the local logic.
 *
 * Most of the responses are returned as ExtractedRemoteResponse which should include the rate limit status
 * if the respective remote response(s) contain(s) them
 */
public class RemoteService implements IRemoteService {
    private static final AppLogger logger = LoggerManager.getLogger(RemoteService.class);
    private static final int RESOURCES_PER_PAGE = 100;

    private final CloudSimulator remote;

    public RemoteService(Config config) {
        remote = new CloudSimulator(config);
    }

    public RemoteService(CloudSimulator cloudSimulator) {
        remote = cloudSimulator;
    }

    /**
     * Checks whether a response from the remote is valid
     *
     * A response is considered valid if the request has successfully executed
     * This does not include the case 304 where it has the same return result as before
     *
     * @param response
     * @return
     */
    public static boolean isValid(RemoteResponse response) {
        switch (response.getResponseCode()) {
            case 200:
            case 201:
            case 202:
            case 203:
            case 204:
                return true;
            default:
                return false;
        }
    }

    /**
     * Gets the list of persons at page pageNumber for addressBookName, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param pageNumber
     * @return wrapped response with list of persons
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<List<Person>> getPersons(String addressBookName, int pageNumber)
            throws IOException {
        RemoteResponse remoteResponse;
        remoteResponse = remote.getPersons(addressBookName, pageNumber, RESOURCES_PER_PAGE, null);
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse);
        }
        List<CloudPerson> cloudPersons = getDataListFromBody(remoteResponse.getBody(), CloudPerson.class);
        return prepareExtractedResponse(remoteResponse, convertToPersonList(cloudPersons));
    }

    /**
     * Gets the list of tags at page pageNumber for addressBookName, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param pageNumber
     * @param previousETag null if there is no previous request
     * @return wrapped response with list of tags
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<List<Tag>> getTags(String addressBookName, int pageNumber, String previousETag)
            throws IOException {
        RemoteResponse remoteResponse = remote.getTags(addressBookName, pageNumber, RESOURCES_PER_PAGE, previousETag);
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse);
        }
        List<CloudTag> cloudTags = getDataListFromBody(remoteResponse.getBody(), CloudTag.class);
        return prepareExtractedResponse(remoteResponse, convertToTagList(cloudTags));
    }

    /**
     * Adds a person to the remote, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param newPerson
     * @return wrapped response of the returned resulting person
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<Person> createPerson(String addressBookName, ReadOnlyPerson newPerson) throws IOException {
        RemoteResponse remoteResponse = remote.createPerson(addressBookName, convertToCloudPerson(newPerson), null);
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse);
        }
        CloudPerson returnedPerson = getDataFromBody(remoteResponse.getBody(), CloudPerson.class);
        return prepareExtractedResponse(remoteResponse, convertToPerson(returnedPerson));
    }

    /**
     * Updates a person on the remote, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param personId
     * @param updatedPerson
     * @return wrapped response of the returned resulting person
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<Person> updatePerson(String addressBookName, int personId, ReadOnlyPerson updatedPerson)
            throws IOException {
        RemoteResponse remoteResponse = remote.updatePerson(addressBookName, personId,
                convertToCloudPerson(updatedPerson), null);
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse);
        }
        CloudPerson returnedPerson = getDataFromBody(remoteResponse.getBody(), CloudPerson.class);
        return prepareExtractedResponse(remoteResponse, convertToPerson(returnedPerson));
    }

    /**
     * Deletes a person on the remote, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param personId
     * @return wrapped response with no additional data
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<Void> deletePerson(String addressBookName, int personId)
            throws IOException {
        RemoteResponse remoteResponse = remote.deletePerson(addressBookName, personId);
        return getResponseWithNoData(remoteResponse);
    }

    /**
     * Creates a tag on the remote, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param tag
     * @return wrapped response of the returned resulting tag
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<Tag> createTag(String addressBookName, Tag tag) throws IOException {
        RemoteResponse remoteResponse = remote.createTag(addressBookName, convertToCloudTag(tag), null);
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse);
        }
        CloudTag returnedTag = getDataFromBody(remoteResponse.getBody(), CloudTag.class);
        return prepareExtractedResponse(remoteResponse, convertToTag(returnedTag));
    }

    /**
     * Updates a tag on the remote
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param oldTagName
     * @param newTag
     * @return wrapped response of the returned resulting tag
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<Tag> editTag(String addressBookName, String oldTagName, Tag newTag)
            throws IOException {
        RemoteResponse remoteResponse = remote.editTag(addressBookName, oldTagName, convertToCloudTag(newTag), null);
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse);
        }
        CloudTag returnedTag = getDataFromBody(remoteResponse.getBody(), CloudTag.class);
        return prepareExtractedResponse(remoteResponse, convertToTag(returnedTag));
    }

    /**
     * Deletes a tag on the remote, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param tagName
     * @return wrapped response with no additional data
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<Void> deleteTag(String addressBookName, String tagName) throws IOException {
        RemoteResponse remoteResponse = remote.deleteTag(addressBookName, tagName);
        return getResponseWithNoData(remoteResponse);
    }

    /**
     * Gets the list of persons at page pageNumber for addressBookName, which have been modified after a certain time,
     * if quota is available.
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param pageNumber
     * @param time non-null LocalDateTime
     * @param previousETag
     * @return wrapped response with the resulting list of persons
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<List<Person>> getUpdatedPersonsSince(String addressBookName, int pageNumber,
                                                                        LocalDateTime time, String previousETag)
            throws IOException {
        RemoteResponse remoteResponse = remote.getUpdatedPersons(addressBookName, time.toString(), pageNumber,
                RESOURCES_PER_PAGE, null);
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse);
        }
        List<CloudPerson> cloudPersons = getDataListFromBody(remoteResponse.getBody(), CloudPerson.class);

        logger.debug("Returning updated persons response for page {}", pageNumber);
        return prepareExtractedResponse(remoteResponse, convertToPersonList(cloudPersons));
    }

    /**
     * Returns the limit status information
     *
     * This does NOT consume API usage.
     *
     * @return wrapped response without additional data
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<HashMap<String, String>> getLimitStatus() throws IOException {
        RemoteResponse remoteResponse = remote.getRateLimitStatus(null);
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse);
        }
        HashMap<String, String> bodyHashMap = getHashMapFromBody(remoteResponse.getBody());
        HashMap<String, String> simplifiedHashMap = getHeaderLimitStatus(bodyHashMap);
        return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(), getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), simplifiedHashMap);
    }

    /**
     * Creates a new addressbook in the remote with name addressBookName
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @return
     * @throws IOException
     */
    @Override
    public ExtractedRemoteResponse<Void> createAddressBook(String addressBookName) throws IOException {
        RemoteResponse remoteResponse = remote.createAddressBook(addressBookName);
        // empty response whether valid response code or not
        return getResponseWithNoData(remoteResponse);
    }

    private <T> ExtractedRemoteResponse<T> prepareExtractedResponse(RemoteResponse remoteResponse, T data) {
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();
        ExtractedRemoteResponse<T> extractedResponse = new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(),
                                            getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), data);
        extractedResponse.setNextPage(remoteResponse.getNextPageNo());
        extractedResponse.setPrevPage(remoteResponse.getPreviousPageNo());
        extractedResponse.setFirstPage(remoteResponse.getFirstPageNo());
        extractedResponse.setLastPage(remoteResponse.getLastPageNo());
        return extractedResponse;
    }

    private HashMap<String, String> getHeaderLimitStatus(HashMap<String, String> bodyHashMap) {
        HashMap<String, String> simplifiedHashMap = new HashMap<>();
        simplifiedHashMap.put("Limit", bodyHashMap.get("X-RateLimit-Limit"));
        simplifiedHashMap.put("Remaining", bodyHashMap.get("X-RateLimit-Remaining"));
        simplifiedHashMap.put("Reset", bodyHashMap.get("X-RateLimit-Reset"));
        return simplifiedHashMap;
    }

    private <V> ExtractedRemoteResponse<V> getResponseWithNoData(RemoteResponse remoteResponse) {
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();
        if (headerHashMap == null || headerHashMap.size() < 3) {
            return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode());
        }
        return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(), getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), null);
    }

    private BufferedReader getReaderForStream(InputStream stream) {
        return new BufferedReader(new InputStreamReader(stream));
    }

    private String convertToString(InputStream stream) throws IOException {
        BufferedReader reader = getReaderForStream(stream);
        StringBuilder stringBuffer = new StringBuilder();
        while (reader.ready()) {
            stringBuffer.append(reader.readLine());
        }

        return stringBuffer.toString();
    }

    /**
     * Parses the stream content and attempts to convert it into an object T
     *
     * @param bodyStream
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    private <T> T getDataFromBody(InputStream bodyStream, Class<T> type) throws IOException {
        return JsonUtil.fromJsonString(convertToString(bodyStream), type);
    }

    /**
     * Parses the stream content and attempts to convert it into a List of object Ts
     *
     * @param bodyStream
     * @param type
     * @param <T>
     * @return
     * @throws IOException
     */
    private <T> List<T> getDataListFromBody(InputStream bodyStream, Class<T> type) throws IOException {
        return JsonUtil.fromJsonStringToList(convertToString(bodyStream), type);
    }

    /**
     * Parses the JSON-formatted stream and attempts to get the header in HashMap<String, String> form
     *
     * @param headerStream
     * @return
     * @throws IOException
     */
    private HashMap<String, String> getHashMapFromBody(InputStream headerStream) throws IOException {
        return JsonUtil.fromJsonStringToHashMap(
                convertToString(headerStream), String.class, String.class);
    }

    private String getETagFromHeader(HashMap<String, String> header) {
        return header.get("ETag");
    }

    private int getRateLimitFromHeader(HashMap<String, String> header) {
        return Integer.parseInt(header.get("X-RateLimit-Limit"));
    }

    private int getRateRemainingFromHeader(HashMap<String, String> header) {
        return Integer.parseInt(header.get("X-RateLimit-Remaining"));
    }

    private long getRateResetFromHeader(HashMap<String, String> header) {
        return Long.parseLong(header.get("X-RateLimit-Reset"));
    }

    private List<Person> convertToPersonList(List<CloudPerson> cloudPersonList) {
        List<Person> convertedList = new ArrayList<>();
        cloudPersonList.stream()
                .forEach(CloudPerson -> convertedList.add(convertToPerson(CloudPerson)));

        return convertedList;
    }

    private List<Tag> convertToTagList(List<CloudTag> cloudTagList) {
        List<Tag> convertedList = new ArrayList<>();
        cloudTagList.stream()
                .forEach(cloudTag -> convertedList.add(convertToTag(cloudTag)));

        return convertedList;
    }

    private List<CloudTag> convertToCloudTagList(List<Tag> tagList) {
        return tagList.stream().map(this::convertToCloudTag).collect(Collectors.toCollection(ArrayList::new));
    }

    private Person convertToPerson(CloudPerson cloudPerson) {
        // TODO: Copy CloudPerson's ID once person ID is implemented
        Person person = new Person(cloudPerson.getFirstName(), cloudPerson.getLastName(), cloudPerson.getId());
        person.setStreet(cloudPerson.getStreet());
        person.setCity(cloudPerson.getCity());
        person.setPostalCode(cloudPerson.getPostalCode());
        person.setTags(convertToTagList(cloudPerson.getTags()));
        person.setBirthday(cloudPerson.getBirthday());
        person.setIsDeleted(cloudPerson.isDeleted());
        return person;
    }

    private CloudPerson convertToCloudPerson(ReadOnlyPerson person) {
        CloudPerson cloudPerson = new CloudPerson(person.getFirstName(), person.getLastName());
        cloudPerson.setStreet(person.getStreet());
        cloudPerson.setCity(person.getCity());
        cloudPerson.setPostalCode(person.getPostalCode());
        cloudPerson.setTags(convertToCloudTagList(person.getTagList()));
        cloudPerson.setBirthday(person.getBirthday());
        cloudPerson.setDeleted(false);
        return cloudPerson;
    }

    private Tag convertToTag(CloudTag cloudTag) {
        return new Tag(cloudTag.getName());
    }

    private CloudTag convertToCloudTag(Tag tag) {
        return new CloudTag(tag.getName());
    }
}
