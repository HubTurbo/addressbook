package address.sync;

import address.model.datatypes.Tag;
import address.model.datatypes.Person;
import address.sync.model.CloudTag;
import address.sync.model.CloudPerson;
import address.util.JsonUtil;
import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

// TODO implement full range of possible unreliable network effects: fail, corruption, etc
// TODO check for bad response code
/**
 * Emulates the cloud & the local cloud service
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
     * Consumes ceil(persons size/RESOURCES_PER_PAGE) API usage
     * @param addressBookName
     * @return wrapped response with list of persons
     * @throws IOException if there is a network error
     */
    @Override
    public ExtractedCloudResponse<List<Person>> getPersons(String addressBookName) throws IOException {
        RawCloudResponse cloudResponse = cloud.getPersons(addressBookName, RESOURCES_PER_PAGE);
        List<CloudPerson> cloudPersons = getDataListFromBody(cloudResponse.getBody(), CloudPerson.class);
        RateLimitStatus rateLimitStatus = getRateLimitStatusFromHeader(cloudResponse.getHeaders());
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), rateLimitStatus, convertToPersonList(cloudPersons));
    }

    /**
     * Gets the list of tags for addressBookName
     *
     * Consumes ceil(tags size/RESOURCES_PER_PAGE) API usage
     * @param addressBookName
     * @return wrapped response with list of tags
     * @throws IOException if there is a network error
     */
    @Override
    public ExtractedCloudResponse<List<Tag>> getTags(String addressBookName) throws IOException {
        RawCloudResponse cloudResponse = cloud.getTags(addressBookName, RESOURCES_PER_PAGE);
        List<CloudTag> cloudTags = getDataListFromBody(cloudResponse.getBody(), CloudTag.class);
        RateLimitStatus rateLimitStatus = getRateLimitStatusFromHeader(cloudResponse.getHeaders());
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), rateLimitStatus, convertToTagList(cloudTags));
    }

    /**
     * Adds a person to the cloud
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param newPerson
     * @return
     * @throws IOException
     */
    @Override
    public ExtractedCloudResponse<Person> createPerson(String addressBookName, Person newPerson) throws IOException {
        RawCloudResponse response = cloud.createPerson(addressBookName, convertToCloudPerson(newPerson));
        CloudPerson returnedPerson = getDataFromBody(response.getBody(), CloudPerson.class);
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), convertToPerson(returnedPerson));
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
     */
    @Override
    public ExtractedCloudResponse<Person> updatePerson(String addressBookName, String oldFirstName, String oldLastName, Person updatedPerson) throws IOException {
        RawCloudResponse response = cloud.updatePerson(addressBookName, oldFirstName, oldLastName, convertToCloudPerson(updatedPerson));
        CloudPerson returnedPerson = getDataFromBody(response.getBody(), CloudPerson.class);
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), convertToPerson(returnedPerson));
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
     * @throws IOException
     */
    @Override
    public ExtractedCloudResponse<Void> deletePerson(String addressBookName, String firstName, String lastName) throws IOException {
        RawCloudResponse response = cloud.deletePerson(addressBookName, firstName, lastName);
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), null);
    }

    /**
     * Creates a tag on the cloud
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param tag
     * @return
     * @throws IOException
     */
    @Override
    public ExtractedCloudResponse<Tag> createTag(String addressBookName, Tag tag) throws IOException {
        RawCloudResponse response = cloud.createTag(addressBookName, convertToCloudTag(tag));
        CloudTag returnedTag = getDataFromBody(response.getBody(), CloudTag.class);
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), convertToTag(returnedTag));
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
     * @throws IOException
     */
    @Override
    public ExtractedCloudResponse<Tag> editTag(String addressBookName, String oldTagName, Tag newTag) throws IOException {
        RawCloudResponse response = cloud.editTag(addressBookName, oldTagName, convertToCloudTag(newTag));
        CloudTag returnedTag = getDataFromBody(response.getBody(), CloudTag.class);
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), convertToTag(returnedTag));
    }

    /**
     * Deletes a tag on the cloud
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param tagName
     * @return
     * @throws IOException
     */
    @Override
    public ExtractedCloudResponse<Void> deleteTag(String addressBookName, String tagName) throws IOException {
        RawCloudResponse response = cloud.deleteTag(addressBookName, tagName);
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), null);
    }

    @Override
    public ExtractedCloudResponse<List<Person>> getUpdatedPersonsSince(String addressBookName, LocalDateTime time) throws IOException {
        RawCloudResponse response = cloud.getUpdatedPersons(addressBookName, time.toString());
        List<CloudPerson> cloudPersons = getDataListFromBody(response.getBody(), CloudPerson.class);
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), convertToPersonList(cloudPersons));
    }

    @Override
    public ExtractedCloudResponse<RateLimitStatus> getLimitStatus() throws IOException {
        RawCloudResponse response = cloud.getRateLimitStatus();
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), null);
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

    private <T> T getDataFromBody(InputStream bodyStream, Class<T> type) throws IOException {
        return JsonUtil.fromJsonString(convertToString(bodyStream), type);
    }

    private <T> List<T> getDataListFromBody(InputStream bodyStream, Class<T> type) throws IOException {
        return JsonUtil.fromJsonStringToList(convertToString(bodyStream), type);
    }

    private RateLimitStatus getRateLimitStatusFromHeader(InputStream headerStream) throws IOException {
        HashMap<String, Long> headers = JsonUtil.fromJsonStringToHashMap(convertToString(headerStream), String.class, Long.class);
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
}
