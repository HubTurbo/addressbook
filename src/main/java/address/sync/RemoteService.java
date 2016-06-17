package address.sync;

import address.model.datatypes.tag.Tag;
import address.model.datatypes.person.Person;
import address.sync.cloud.CloudSimulator;
import address.sync.cloud.CloudResponse;
import address.sync.model.RemotePerson;
import address.sync.model.RemoteTag;
import address.util.JsonUtil;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * This component is responsible for providing a high-level API for communication with the remote,
 * as well as the reformatting the remote's response into a format understood by the local logic.
 *
 * Most of the responses returned should include the rate limit status if the respective remote response(s)
 * contain(s) them
 */
public class RemoteService implements IRemoteService {
    private static final int RESOURCES_PER_PAGE = 100;

    private final CloudSimulator remote;

    public RemoteService(boolean shouldSimulateUnreliableNetwork) {
        remote = new CloudSimulator(shouldSimulateUnreliableNetwork);
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
    public static boolean isValid(CloudResponse response) {
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
     * Gets the list of persons for addressBookName, if quota is available
     *
     * Consumes 1 + floor(persons size/RESOURCES_PER_PAGE) API usage
     * @param addressBookName
     * @return wrapped response with list of persons
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<List<Person>> getPersons(String addressBookName) throws IOException {
        int curPageNumber = 1;
        CloudResponse remoteResponse;
        List<RemotePerson> remotePersons = new ArrayList<>();
        do {
            remoteResponse = remote.getPersons(addressBookName, curPageNumber, RESOURCES_PER_PAGE, null);
            if (!isValid(remoteResponse)) {
                return getResponseWithNoData(remoteResponse, remoteResponse.getHeaders());
            }
            remotePersons.addAll(getDataListFromBody(remoteResponse.getBody(), RemotePerson.class));
            curPageNumber++;
        } while (remoteResponse.getNextPageNo() != -1);

        // Use the header of the last request, which contains the latest API rate limit
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();
        return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(), getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap),
                                            convertToPersonList(remotePersons));
    }

    /**
     * Gets the list of tags for addressBookName, if quota is available
     *
     * Consumes 1 + floor(tags size/RESOURCES_PER_PAGE) API usage
     * @param addressBookName
     * @param previousETag null if there is no previous request
     * @return wrapped response with list of tags
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<List<Tag>> getTags(String addressBookName, String previousETag) throws IOException {
        int curPageNumber = 1;
        CloudResponse remoteResponse;
        List<RemoteTag> remoteTags = new ArrayList<>();
        do {
            remoteResponse = remote.getTags(addressBookName, curPageNumber, RESOURCES_PER_PAGE, previousETag);
            if (!isValid(remoteResponse)) {
                return getResponseWithNoData(remoteResponse, remoteResponse.getHeaders());
            }
            remoteTags.addAll(getDataListFromBody(remoteResponse.getBody(), RemoteTag.class));
            curPageNumber++;
        } while (remoteResponse.getNextPageNo() != -1);

        // Use the header of the last request, which contains the latest API rate limit
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();

        return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(), getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), convertToTagList(remoteTags));
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
    public ExtractedRemoteResponse<Person> createPerson(String addressBookName, Person newPerson) throws IOException {
        CloudResponse remoteResponse = remote.createPerson(addressBookName, convertToRemotePerson(newPerson), null);
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse, headerHashMap);
        }
        RemotePerson returnedPerson = getDataFromBody(remoteResponse.getBody(), RemotePerson.class);
        return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(), getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), convertToPerson(returnedPerson));
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
    public ExtractedRemoteResponse<Person> updatePerson(String addressBookName, int personId, Person updatedPerson)
            throws IOException {
        CloudResponse remoteResponse = remote.updatePerson(addressBookName, personId,
                convertToRemotePerson(updatedPerson), null);
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse, headerHashMap);
        }
        RemotePerson returnedPerson = getDataFromBody(remoteResponse.getBody(), RemotePerson.class);
        return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(), getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap),
                                            convertToPerson(returnedPerson));
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
        CloudResponse remoteResponse = remote.deletePerson(addressBookName, personId);
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse, headerHashMap);
        }
        return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(), getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), null);
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
        CloudResponse remoteResponse = remote.createTag(addressBookName, convertToRemoteTag(tag), null);
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse, headerHashMap);
        }
        RemoteTag returnedTag = getDataFromBody(remoteResponse.getBody(), RemoteTag.class);
        return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(), getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), convertToTag(returnedTag));
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
        CloudResponse remoteResponse = remote.editTag(addressBookName, oldTagName, convertToRemoteTag(newTag), null);
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse, headerHashMap);
        }
        RemoteTag returnedTag = getDataFromBody(remoteResponse.getBody(), RemoteTag.class);
        return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(), getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap),
                                            convertToTag(returnedTag));
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
        CloudResponse remoteResponse = remote.deleteTag(addressBookName, tagName);
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse, headerHashMap);
        }
        return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(), getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), null);
    }

    /**
     * Gets the list of persons for addressBookName, which have been modified after a certain time,
     * if quota is available.
     * Parameter time should not be null.
     *
     * Consumes 1 + floor(result size/RESOURCES_PER_PAGE) API usage
     *
     * @param addressBookName
     * @return wrapped response with the resulting list of persons
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedRemoteResponse<List<Person>> getUpdatedPersonsSince(String addressBookName, LocalDateTime time)
            throws IOException {
        int curPageNumber = 1;
        CloudResponse remoteResponse;
        List<RemotePerson> remotePersons = new ArrayList<>();
        do {
            remoteResponse = remote.getUpdatedPersons(addressBookName, time.toString(), curPageNumber,
                    RESOURCES_PER_PAGE, null);
            if (!isValid(remoteResponse)) {
                return getResponseWithNoData(remoteResponse, remoteResponse.getHeaders());
            }
            remotePersons.addAll(getDataListFromBody(remoteResponse.getBody(), RemotePerson.class));
            curPageNumber++;
        } while (remoteResponse.getNextPageNo() != -1);

        // Use the header of the last request, which contains the latest API rate limit
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();

        return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(), getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), convertToPersonList(remotePersons));
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
        CloudResponse remoteResponse = remote.getRateLimitStatus(null);
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();
        if (!isValid(remoteResponse)) {
            return getResponseWithNoData(remoteResponse, headerHashMap);
        }
        HashMap<String, String> bodyHashMap = getHashMapFromBody(remoteResponse.getBody());
        HashMap<String, String> simplifiedHashMap = getHeaderLimitStatus(bodyHashMap);
        return new ExtractedRemoteResponse<>(remoteResponse.getResponseCode(), getETagFromHeader(headerHashMap),
                                            getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), simplifiedHashMap);
    }

    private HashMap<String, String> getHeaderLimitStatus(HashMap<String, String> bodyHashMap) {
        HashMap<String, String> simplifiedHashMap = new HashMap<>();
        simplifiedHashMap.put("Limit", bodyHashMap.get("X-RateLimit-Limit"));
        simplifiedHashMap.put("Remaining", bodyHashMap.get("X-RateLimit-Remaining"));
        simplifiedHashMap.put("Reset", bodyHashMap.get("X-RateLimit-Reset"));
        return simplifiedHashMap;
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
        CloudResponse remoteResponse = remote.createAddressBook(addressBookName);
        HashMap<String, String> headerHashMap = remoteResponse.getHeaders();

        // empty response whether valid response code or not
        return getResponseWithNoData(remoteResponse, headerHashMap);
    }

    private <V> ExtractedRemoteResponse<V> getResponseWithNoData(CloudResponse remoteResponse,
                                                                 HashMap<String, String> headerHashMap) {
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

    private List<Person> convertToPersonList(List<RemotePerson> remotePersonList) {
        List<Person> convertedList = new ArrayList<>();
        remotePersonList.stream()
                .forEach(remotePerson -> convertedList.add(convertToPerson(remotePerson)));

        return convertedList;
    }

    private List<Tag> convertToTagList(List<RemoteTag> remoteTagList) {
        List<Tag> convertedList = new ArrayList<>();
        remoteTagList.stream()
                .forEach(remoteTag -> convertedList.add(convertToTag(remoteTag)));

        return convertedList;
    }

    private List<RemoteTag> convertToRemoteTagList(List<Tag> tagList) {
        List<RemoteTag> convertedList = new ArrayList<>();
        tagList.stream()
                .forEach(tag -> convertedList.add(convertToRemoteTag(tag)));

        return convertedList;
    }

    private Person convertToPerson(RemotePerson remotePerson) {
        // TODO: Copy remotePerson's ID
        Person person = new Person(remotePerson.getFirstName(), remotePerson.getLastName());
        person.setStreet(remotePerson.getStreet());
        person.setCity(remotePerson.getCity());
        person.setPostalCode(remotePerson.getPostalCode());
        person.setTags(convertToTagList(remotePerson.getTags()));
        person.setBirthday(remotePerson.getBirthday());
        return person;
    }

    private RemotePerson convertToRemotePerson(Person person) {
        RemotePerson remotePerson = new RemotePerson(person.getFirstName(), person.getLastName());
        remotePerson.setStreet(person.getStreet());
        remotePerson.setCity(person.getCity());
        remotePerson.setPostalCode(person.getPostalCode());
        remotePerson.setTags(convertToRemoteTagList(person.getTags()));
        remotePerson.setBirthday(person.getBirthday());
        return remotePerson;
    }

    private Tag convertToTag(RemoteTag remoteTag) {
        return new Tag(remoteTag.getName());
    }

    private RemoteTag convertToRemoteTag(Tag tag) {
        return new RemoteTag(tag.getName());
    }
}
