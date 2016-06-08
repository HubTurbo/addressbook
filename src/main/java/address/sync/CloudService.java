package address.sync;

import address.model.datatypes.Tag;
import address.model.datatypes.Person;
import address.sync.model.CloudTag;
import address.sync.model.CloudPerson;
import address.util.JsonUtil;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Emulates the local cloud service
 *
 * This component is responsible for providing a high-level API for communication with the cloud,
 * as well as the reformatting the cloud's response into a format understood by the local logic.
 *
 * Most of the responses returned should include the rate limit status if the respective cloud response(s)
 * contain(s) them
 */
public class CloudService implements ICloudService {
    private static final int RESOURCES_PER_PAGE = 100;

    private final CloudSimulator cloud;

    public CloudService(boolean shouldSimulateUnreliableNetwork) {
        cloud = new CloudSimulator(shouldSimulateUnreliableNetwork);
    }

    public CloudService(CloudSimulator cloudSimulator) {
        cloud = cloudSimulator;
    }

    /**
     * Checks whether a response from the cloud is valid
     *
     * A response is considered valid if the request has successfully executed
     * This does not include the case 304 where it has the same return result as before
     *
     * @param response
     * @return
     */
    public static boolean isValid(RawCloudResponse response) {
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
    public ExtractedCloudResponse<List<Person>> getPersons(String addressBookName) throws IOException {
        int curPageNumber = 1;
        RawCloudResponse cloudResponse;
        List<CloudPerson> cloudPersons = new ArrayList<>();
        do {
            cloudResponse = cloud.getPersons(addressBookName, curPageNumber, RESOURCES_PER_PAGE, null);
            if (!isValid(cloudResponse)) {
                return getResponseWithNoData(cloudResponse, cloudResponse.getHeaders());
            }
            cloudPersons.addAll(getDataListFromBody(cloudResponse.getBody(), CloudPerson.class));
            curPageNumber++;
        } while (cloudResponse.getNextPageNo() != -1);

        // Use the header of the last request, which contains the latest API rate limit
        HashMap<String, String> headerHashMap = cloudResponse.getHeaders();

        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap),
                                            convertToPersonList(cloudPersons));
    }

    /**
     * Gets the list of tags for addressBookName, if quota is available
     *
     * Consumes 1 + floor(tags size/RESOURCES_PER_PAGE) API usage
     * @param addressBookName
     * @return wrapped response with list of tags
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<List<Tag>> getTags(String addressBookName) throws IOException {
        int curPageNumber = 1;
        RawCloudResponse cloudResponse;
        List<CloudTag> cloudTags = new ArrayList<>();
        do {
            cloudResponse = cloud.getTags(addressBookName, curPageNumber, RESOURCES_PER_PAGE, null);
            if (!isValid(cloudResponse)) {
                return getResponseWithNoData(cloudResponse, cloudResponse.getHeaders());
            }
            cloudTags.addAll(getDataListFromBody(cloudResponse.getBody(), CloudTag.class));
            curPageNumber++;
        } while (cloudResponse.getNextPageNo() != -1);

        // Use the header of the last request, which contains the latest API rate limit
        HashMap<String, String> headerHashMap = cloudResponse.getHeaders();

        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), convertToTagList(cloudTags));
    }

    /**
     * Adds a person to the cloud, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param newPerson
     * @return wrapped response of the returned resulting person
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<Person> createPerson(String addressBookName, Person newPerson) throws IOException {
        RawCloudResponse cloudResponse = cloud.createPerson(addressBookName, convertToCloudPerson(newPerson), null);
        HashMap<String, String> headerHashMap = cloudResponse.getHeaders();
        if (!isValid(cloudResponse)) {
            return getResponseWithNoData(cloudResponse, headerHashMap);
        }
        CloudPerson returnedPerson = getDataFromBody(cloudResponse.getBody(), CloudPerson.class);
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), convertToPerson(returnedPerson));
    }

    /**
     * Updates a person on the cloud, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param oldFirstName
     * @param oldLastName
     * @param updatedPerson
     * @return wrapped response of the returned resulting person
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<Person> updatePerson(String addressBookName, String oldFirstName, String oldLastName, Person updatedPerson) throws IOException {
        RawCloudResponse cloudResponse = cloud.updatePerson(addressBookName, oldFirstName, oldLastName, convertToCloudPerson(updatedPerson), null);
        HashMap<String, String> headerHashMap = cloudResponse.getHeaders();
        if (!isValid(cloudResponse)) {
            return getResponseWithNoData(cloudResponse, headerHashMap);
        }
        CloudPerson returnedPerson = getDataFromBody(cloudResponse.getBody(), CloudPerson.class);
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap),
                                            convertToPerson(returnedPerson));
    }

    /**
     * Deletes a person on the cloud, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param firstName
     * @param lastName
     * @return wrapped response with no additional data
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<Void> deletePerson(String addressBookName, String firstName, String lastName)
            throws IOException {
        RawCloudResponse cloudResponse = cloud.deletePerson(addressBookName, firstName, lastName);
        HashMap<String, String> headerHashMap = cloudResponse.getHeaders();
        if (!isValid(cloudResponse)) {
            return getResponseWithNoData(cloudResponse, headerHashMap);
        }
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), null);
    }

    /**
     * Creates a tag on the cloud, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param tag
     * @return wrapped response of the returned resulting tag
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<Tag> createTag(String addressBookName, Tag tag) throws IOException {
        RawCloudResponse cloudResponse = cloud.createTag(addressBookName, convertToCloudTag(tag), null);
        HashMap<String, String> headerHashMap = cloudResponse.getHeaders();
        if (!isValid(cloudResponse)) {
            return getResponseWithNoData(cloudResponse, headerHashMap);
        }
        CloudTag returnedTag = getDataFromBody(cloudResponse.getBody(), CloudTag.class);
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), convertToTag(returnedTag));
    }

    /**
     * Updates a tag on the cloud
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
    public ExtractedCloudResponse<Tag> editTag(String addressBookName, String oldTagName, Tag newTag)
            throws IOException {
        RawCloudResponse cloudResponse = cloud.editTag(addressBookName, oldTagName, convertToCloudTag(newTag), null);
        HashMap<String, String> headerHashMap = cloudResponse.getHeaders();
        if (!isValid(cloudResponse)) {
            return getResponseWithNoData(cloudResponse, headerHashMap);
        }
        CloudTag returnedTag = getDataFromBody(cloudResponse.getBody(), CloudTag.class);
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap),
                                            convertToTag(returnedTag));
    }

    /**
     * Deletes a tag on the cloud, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param tagName
     * @return wrapped response with no additional data
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<Void> deleteTag(String addressBookName, String tagName) throws IOException {
        RawCloudResponse cloudResponse = cloud.deleteTag(addressBookName, tagName);
        HashMap<String, String> headerHashMap = cloudResponse.getHeaders();
        if (!isValid(cloudResponse)) {
            return getResponseWithNoData(cloudResponse, headerHashMap);
        }
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), null);
    }

    /**
     * Gets the list of persons for addressBookName, which have been modified after a certain time,
     * if quota is available
     *
     * Consumes 1 + floor(result size/RESOURCES_PER_PAGE) API usage
     *
     * @param addressBookName
     * @return wrapped response with the resulting list of persons
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<List<Person>> getUpdatedPersonsSince(String addressBookName, LocalDateTime time)
            throws IOException {
        int curPageNumber = 1;
        RawCloudResponse cloudResponse;
        List<CloudPerson> cloudPersons = new ArrayList<>();
        do {
            cloudResponse = cloud.getUpdatedPersons(addressBookName, time.toString(), curPageNumber, RESOURCES_PER_PAGE, null);
            if (!isValid(cloudResponse)) {
                return getResponseWithNoData(cloudResponse, cloudResponse.getHeaders());
            }
            cloudPersons.addAll(getDataListFromBody(cloudResponse.getBody(), CloudPerson.class));
            curPageNumber++;
        } while (cloudResponse.getNextPageNo() != -1);

        // Use the header of the last request, which contains the latest API rate limit
        HashMap<String, String> headerHashMap = cloudResponse.getHeaders();

        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), convertToPersonList(cloudPersons));
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
    public ExtractedCloudResponse<HashMap<String, String>> getLimitStatus() throws IOException {
        RawCloudResponse cloudResponse = cloud.getRateLimitStatus(null);
        HashMap<String, String> headerHashMap = cloudResponse.getHeaders();
        if (!isValid(cloudResponse)) {
            return getResponseWithNoData(cloudResponse, headerHashMap);
        }
        HashMap<String, String> bodyHashMap = getHashMapFromBody(cloudResponse.getBody());
        HashMap<String, String> simplifiedHashMap = simplifyHashMap(bodyHashMap);
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), getRateLimitFromHeader(headerHashMap),
                                            getRateRemainingFromHeader(headerHashMap),
                                            getRateResetFromHeader(headerHashMap), simplifiedHashMap);
    }

    private HashMap<String, String> simplifyHashMap(HashMap<String, String> bodyHashMap) {
        HashMap<String, String> simplifiedHashMap = new HashMap<>();
        simplifiedHashMap.put("Limit", bodyHashMap.get("X-RateLimit-Limit"));
        simplifiedHashMap.put("Remaining", bodyHashMap.get("X-RateLimit-Remaining"));
        simplifiedHashMap.put("Reset", bodyHashMap.get("X-RateLimit-Reset"));
        return simplifiedHashMap;
    }

    /**
     * Creates a new addressbook in the cloud with name addressBookName
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @return
     * @throws IOException
     */
    @Override
    public ExtractedCloudResponse<Void> createAddressBook(String addressBookName) throws IOException {
        RawCloudResponse cloudResponse = cloud.createAddressBook(addressBookName);
        HashMap<String, String> headerHashMap = cloudResponse.getHeaders();

        // empty response whether valid response code or not
        return getResponseWithNoData(cloudResponse, headerHashMap);
    }

    private <V> ExtractedCloudResponse<V> getResponseWithNoData(RawCloudResponse cloudResponse,
                                                                HashMap<String, String> headerHashMap) {
        if (headerHashMap == null || headerHashMap.size() < 3) {
            return new ExtractedCloudResponse<>(cloudResponse.getResponseCode());
        }
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), getRateLimitFromHeader(headerHashMap),
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
                .forEach(cloudPerson -> convertedList.add(convertToPerson(cloudPerson)));

        return convertedList;
    }

    private List<Tag> convertToTagList(List<CloudTag> cloudTagList) {
        List<Tag> convertedList = new ArrayList<>();
        cloudTagList.stream()
                .forEach(cloudTag -> convertedList.add(convertToTag(cloudTag)));

        return convertedList;
    }

    private List<CloudTag> convertToCloudTagList(List<Tag> tagList) {
        List<CloudTag> convertedList = new ArrayList<>();
        tagList.stream()
                .forEach(tag -> convertedList.add(convertToCloudTag(tag)));

        return convertedList;
    }

    private Person convertToPerson(CloudPerson cloudPerson) {
        Person person = new Person(cloudPerson.getFirstName(), cloudPerson.getLastName());
        person.setStreet(cloudPerson.getStreet());
        person.setCity(cloudPerson.getCity());
        person.setPostalCode(cloudPerson.getPostalCode());
        person.setTags(convertToTagList(cloudPerson.getTags()));
        person.setBirthday(cloudPerson.getBirthday());
        return person;
    }

    private CloudPerson convertToCloudPerson(Person person) {
        CloudPerson cloudPerson = new CloudPerson(person.getFirstName(), person.getLastName());
        cloudPerson.setStreet(person.getStreet());
        cloudPerson.setCity(person.getCity());
        cloudPerson.setPostalCode(person.getPostalCode());
        cloudPerson.setTags(convertToCloudTagList(person.getTags()));
        cloudPerson.setBirthday(person.getBirthday());
        return cloudPerson;
    }

    private Tag convertToTag(CloudTag cloudTag) {
        return new Tag(cloudTag.getName());
    }

    private CloudTag convertToCloudTag(Tag tag) {
        return new CloudTag(tag.getName());
    }
}
