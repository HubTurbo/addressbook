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
 * as well as the reformatting the cloud's response into a format understood by the local logic
 */
public class CloudService implements ICloudService {
    private static final int RESOURCES_PER_PAGE = 100;

    private final CloudSimulator cloud;


    public CloudService(boolean shouldSimulateUnreliableNetwork) {
        cloud = new CloudSimulator(shouldSimulateUnreliableNetwork);
    }

    public static void main(String[] args) {
        CloudService test = new CloudService(false);
    }

    /**
     * Gets the list of persons for addressBookName
     *
     * Consumes 1 + floor(persons size/RESOURCES_PER_PAGE) API usage
     * @param addressBookName
     * @return wrapped response with list of persons
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<List<Person>> getPersons(String addressBookName) throws IOException {
        RawCloudResponse cloudResponse = cloud.getPersons(addressBookName, RESOURCES_PER_PAGE);
        if (!isValid(cloudResponse)) {
            return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                                                getRateLimitStatusFromHeader(cloudResponse.getHeaders()), null);
        }
        List<CloudPerson> cloudPersons = getDataListFromBody(cloudResponse.getBody(), CloudPerson.class);
        RateLimitStatus rateLimitStatus = getRateLimitStatusFromHeader(cloudResponse.getHeaders());
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), rateLimitStatus,
                                            convertToPersonList(cloudPersons));
    }

    /**
     * Gets the list of tags for addressBookName
     *
     * Consumes 1 + floor(tags size/RESOURCES_PER_PAGE) API usage
     * @param addressBookName
     * @return wrapped response with list of tags
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<List<Tag>> getTags(String addressBookName) throws IOException {
        RawCloudResponse cloudResponse = cloud.getTags(addressBookName, RESOURCES_PER_PAGE);
        if (!isValid(cloudResponse)) {
            return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                    getRateLimitStatusFromHeader(cloudResponse.getHeaders()), null);
        }
        List<CloudTag> cloudTags = getDataListFromBody(cloudResponse.getBody(), CloudTag.class);
        RateLimitStatus rateLimitStatus = getRateLimitStatusFromHeader(cloudResponse.getHeaders());
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), rateLimitStatus,
                                            convertToTagList(cloudTags));
    }

    /**
     * Adds a person to the cloud
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param newPerson
     * @return
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<Person> createPerson(String addressBookName, Person newPerson) throws IOException {
        RawCloudResponse cloudResponse = cloud.createPerson(addressBookName, convertToCloudPerson(newPerson));
        if (!isValid(cloudResponse)) {
            return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                    getRateLimitStatusFromHeader(cloudResponse.getHeaders()), null);
        }
        CloudPerson returnedPerson = getDataFromBody(cloudResponse.getBody(), CloudPerson.class);
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                                            getRateLimitStatusFromHeader(cloudResponse.getHeaders()),
                                            convertToPerson(returnedPerson));
    }

    /**
     * Updates a person on the cloud
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param oldFirstName
     * @param oldLastName
     * @param updatedPerson
     * @return
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<Person> updatePerson(String addressBookName, String oldFirstName, String oldLastName, Person updatedPerson) throws IOException {
        RawCloudResponse cloudResponse = cloud.updatePerson(addressBookName, oldFirstName, oldLastName, convertToCloudPerson(updatedPerson));
        if (!isValid(cloudResponse)) {
            return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                    getRateLimitStatusFromHeader(cloudResponse.getHeaders()), null);
        }
        CloudPerson returnedPerson = getDataFromBody(cloudResponse.getBody(), CloudPerson.class);
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                                            getRateLimitStatusFromHeader(cloudResponse.getHeaders()),
                                            convertToPerson(returnedPerson));
    }

    /**
     * Deletes a person on the cloud
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param firstName
     * @param lastName
     * @return
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<Void> deletePerson(String addressBookName, String firstName, String lastName)
            throws IOException {
        RawCloudResponse cloudResponse = cloud.deletePerson(addressBookName, firstName, lastName);
        if (!isValid(cloudResponse)) {
            return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                    getRateLimitStatusFromHeader(cloudResponse.getHeaders()), null);
        }
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                                            getRateLimitStatusFromHeader(cloudResponse.getHeaders()),
                                            null);
    }

    /**
     * Creates a tag on the cloud
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param tag
     * @return
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<Tag> createTag(String addressBookName, Tag tag) throws IOException {
        RawCloudResponse cloudResponse = cloud.createTag(addressBookName, convertToCloudTag(tag));
        if (!isValid(cloudResponse)) {
            return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                    getRateLimitStatusFromHeader(cloudResponse.getHeaders()), null);
        }
        CloudTag returnedTag = getDataFromBody(cloudResponse.getBody(), CloudTag.class);
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                                            getRateLimitStatusFromHeader(cloudResponse.getHeaders()),
                                            convertToTag(returnedTag));
    }

    /**
     * Updates a tag on the cloud
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param oldTagName
     * @param newTag
     * @return
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<Tag> editTag(String addressBookName, String oldTagName, Tag newTag)
            throws IOException {
        RawCloudResponse cloudResponse = cloud.editTag(addressBookName, oldTagName, convertToCloudTag(newTag));
        if (!isValid(cloudResponse)) {
            return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                    getRateLimitStatusFromHeader(cloudResponse.getHeaders()), null);
        }
        CloudTag returnedTag = getDataFromBody(cloudResponse.getBody(), CloudTag.class);
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                                            getRateLimitStatusFromHeader(cloudResponse.getHeaders()),
                                            convertToTag(returnedTag));
    }

    /**
     * Deletes a tag on the cloud
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param tagName
     * @return
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<Void> deleteTag(String addressBookName, String tagName) throws IOException {
        RawCloudResponse cloudResponse = cloud.deleteTag(addressBookName, tagName);
        if (!isValid(cloudResponse)) {
            return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                    getRateLimitStatusFromHeader(cloudResponse.getHeaders()), null);
        }
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                                            getRateLimitStatusFromHeader(cloudResponse.getHeaders()), null);
    }

    /**
     * Gets the list of persons for addressBookName, which have been modified after a certain time
     *
     * Consumes 1 + floor(result size/RESOURCES_PER_PAGE) API usage
     * @param addressBookName
     * @return wrapped response with list of persons
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<List<Person>> getUpdatedPersonsSince(String addressBookName, LocalDateTime time)
            throws IOException {
        RawCloudResponse cloudResponse = cloud.getUpdatedPersons(addressBookName, time.toString());
        if (!isValid(cloudResponse)) {
            return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                    getRateLimitStatusFromHeader(cloudResponse.getHeaders()), null);
        }
        List<CloudPerson> cloudPersons = getDataListFromBody(cloudResponse.getBody(), CloudPerson.class);
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                                            getRateLimitStatusFromHeader(cloudResponse.getHeaders()),
                                            convertToPersonList(cloudPersons));
    }

    /**
     * Returns the limit status information
     *
     * This does NOT consume API usage.
     *
     * @return
     * @throws IOException if content cannot be interpreted
     */
    @Override
    public ExtractedCloudResponse<RateLimitStatus> getLimitStatus() throws IOException {
        RawCloudResponse cloudResponse = cloud.getRateLimitStatus();
        if (!isValid(cloudResponse)) {
            return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                    getRateLimitStatusFromHeader(cloudResponse.getHeaders()), null);
        }
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(),
                                            getRateLimitStatusFromHeader(cloudResponse.getHeaders()),
                                            null);
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
     * Parses the stream and attempts to get the rate limit status
     * The stream should contain a HashMap<String, Long> in JSON format
     *
     * @param headerStream
     * @return
     * @throws IOException
     */
    private RateLimitStatus getRateLimitStatusFromHeader(InputStream headerStream) throws IOException {
        HashMap<String, Long> headers = JsonUtil.fromJsonStringToHashMap(
                convertToString(headerStream), String.class, Long.class);
        return new RateLimitStatus(
                headers.get("X-RateLimit-Limit").intValue(),
                headers.get("X-RateLimit-Remaining").intValue(),
                headers.get("X-RateLimit-Reset"));
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

    private Person convertToPerson(CloudPerson cloudPerson) {
        Person person = new Person(cloudPerson.getFirstName(), cloudPerson.getLastName());
        person.setStreet(cloudPerson.getStreet());
        person.setCity(cloudPerson.getCity());
        person.setPostalCode(cloudPerson.getPostalCode());
        return person;
    }

    private CloudPerson convertToCloudPerson(Person person) {
        CloudPerson cloudPerson = new CloudPerson(person.getFirstName(), person.getLastName());
        cloudPerson.setStreet(person.getStreet());
        cloudPerson.setCity(person.getCity());
        cloudPerson.setPostalCode(person.getPostalCode());
        return cloudPerson;
    }

    private Tag convertToTag(CloudTag cloudTag) {
        return new Tag(cloudTag.getName());
    }

    private CloudTag convertToCloudTag(Tag tag) {
        return new CloudTag(tag.getName());
    }

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
}
