package address.sync;

import address.model.ContactGroup;
import address.model.Person;
import address.sync.model.CloudGroup;
import address.sync.model.CloudPerson;
import address.util.JsonUtil;
import address.util.XmlFileHelper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.reflect.TypeToken;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// TODO implement full range of possible unreliable network effects: fail, corruption, etc
// TODO check for bad response code
/**
 * Emulates the cloud & the local cloud service
 */
public class CloudService implements ICloudService {
    private static final int RESOURCES_PER_PAGE = 100;

    private CloudSimulator cloud;


    public CloudService(boolean shouldSimulateUnreliableNetwork) {
        cloud = new CloudSimulator(shouldSimulateUnreliableNetwork);
    }

    public static void main(String[] args) {
        CloudService test = new CloudService(false);
    }

    /**
     * Requests the simulated cloud to update its data with the given data. This data should be
     * written to the provided mirror file
     * @param delay Duration of delay in seconds to be simulated before the request is completed
     */
    public void requestChangesToCloud(File file, List<Person> people, List<ContactGroup> groups, int delay)
            throws JAXBException {
        if (file == null) return;
        List<Person> newPeople = people.stream().map(Person::new).collect(Collectors.toList());
        List<ContactGroup> newGroups = groups.stream().map(ContactGroup::new).collect(Collectors.toList());
        XmlFileHelper.saveDataToFile(file, newPeople, newGroups);
        try {
            TimeUnit.SECONDS.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

        System.out.println("Body: " + cloudResponse.getBody().toString());
        List<Person> persons = getDataListFromBody(cloudResponse.getBody(), Person.class);
        RateLimitStatus rateLimitStatus = getRateLimitStatusFromHeader(cloudResponse.getHeaders());
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), rateLimitStatus, persons);
    }

    /**
     * Gets the list of contact groups for addressBookName
     *
     * Consumes ceil(group size/RESOURCES_PER_PAGE) API usage
     * @param addressBookName
     * @return wrapped response with list of contact groups
     * @throws IOException if there is a network error
     */
    @Override
    public ExtractedCloudResponse<List<ContactGroup>> getGroups(String addressBookName) throws IOException {
        RawCloudResponse cloudResponse = cloud.getGroups(addressBookName, RESOURCES_PER_PAGE);

        List<ContactGroup> groups = getDataListFromBody(cloudResponse.getBody(), ContactGroup.class);
        RateLimitStatus rateLimitStatus = getRateLimitStatusFromHeader(cloudResponse.getHeaders());
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), rateLimitStatus, groups);
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
        Person returnedPerson = getDataFromBody(response.getBody(), Person.class);
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), returnedPerson);
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
     * Creates a group on the cloud
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param group
     * @return
     * @throws IOException
     */
    @Override
    public ExtractedCloudResponse<ContactGroup> createGroup(String addressBookName, ContactGroup group) throws IOException {
        RawCloudResponse response = cloud.createGroup(addressBookName, convertToCloudGroup(group));
        ContactGroup returnedGroup = getDataFromBody(response.getBody(), ContactGroup.class);
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), returnedGroup);
    }

    /**
     * Updates a group on the cloud
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param oldGroupName
     * @param newGroup
     * @return
     * @throws IOException
     */
    @Override
    public ExtractedCloudResponse<ContactGroup> editGroup(String addressBookName, String oldGroupName, ContactGroup newGroup) throws IOException {
        RawCloudResponse response = cloud.editGroup(addressBookName, oldGroupName, convertToCloudGroup(newGroup));
        ContactGroup returnedGroup = getDataFromBody(response.getBody(), ContactGroup.class);
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), returnedGroup);
    }

    /**
     * Deletes a group on the cloud
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param groupName
     * @return
     * @throws IOException
     */
    @Override
    public ExtractedCloudResponse<Void> deleteGroup(String addressBookName, String groupName) throws IOException {
        RawCloudResponse response = cloud.deleteGroup(addressBookName, groupName);
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), null);
    }

    @Override
    public ExtractedCloudResponse<List<Person>> getUpdatedPersons(String addressBookName) {
        return null;
    }

    @Override
    public ExtractedCloudResponse<List<ContactGroup>> getUpdatedGroups(String addressBookName) {
        return null;
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
        StringBuffer stringBuffer = new StringBuffer();
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
        cloudPerson.setPostalCode(person.getPostalCode().toString());
        return cloudPerson;
    }

    private ContactGroup convertToGroup(CloudGroup cloudGroup) {
        ContactGroup group = new ContactGroup(cloudGroup.getName());
        return group;
    }

    private CloudGroup convertToCloudGroup(ContactGroup group) {
        CloudGroup cloudGroup = new CloudGroup(group.getName());
        return cloudGroup;
    }
}
