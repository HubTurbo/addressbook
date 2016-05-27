package address.sync;

import address.model.AddressBook;
import address.model.ContactGroup;
import address.model.Person;
import address.sync.model.CloudGroup;
import address.sync.model.CloudPerson;
import address.util.XmlFileHelper;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.google.gson.stream.JsonToken.BEGIN_ARRAY;

// TODO implement full range of possible unreliable network effects: fail, corruption, etc
// TODO check for bad response code
/**
 * Emulates the cloud & the local cloud service
 */
public class CloudService implements ICloudService {
    private static final double FAILURE_PROBABILITY = 0.1;

    private static final int MIN_DELAY_IN_SEC = 1;
    private static final int DELAY_RANGE = 5;

    private static final double MODIFY_PERSON_PROBABILITY = 0.1;
    private static final double ADD_PERSON_PROBABILITY = 0.05;
    private static final int MAX_NUM_PERSONS_TO_ADD = 2;
    private static final Random RANDOM_GENERATOR = new Random();
    private static final int RESOURCES_PER_PAGE = 100;

    private Gson gson;
    private boolean simulateUnreliableNetwork;
    private CloudSimulator cloud;


    public CloudService(boolean shouldSimulateUnreliableNetwork) {
        gson = new Gson();
        simulateUnreliableNetwork = shouldSimulateUnreliableNetwork;
        cloud = new CloudSimulator();
    }



    public static void main(String[] args) {
        CloudService test = new CloudService(false);
    }

    /**
     * Gets the updated data subjected to simulated random behaviors such as random changes,
     * random delays and random failures. The data is originally obtained from a given file.
     * The data is possibly modified in each call to this method and is persisted onto the same file.
     * When failure condition occurs, this returns an empty optional
     *
     * @return optional wrapping the (possibly corrupted) data, or empty if retrieving failed.
     */
    public Optional<AddressBook> getSimulatedCloudData(File cloudFile) {
        System.out.println("Simulating cloud data retrieval...");
        try {
            AddressBook modifiedData;
            AddressBook data = XmlFileHelper.getDataFromFile(cloudFile);
            if (!this.simulateUnreliableNetwork) {
                return Optional.of(data);
            }

            // no data could be retrieved
            if (RANDOM_GENERATOR.nextDouble() <= FAILURE_PROBABILITY) {
                System.out.println("Cloud simulator: failure occurred! Could not retrieve data");
                return Optional.empty();
            }

            modifiedData = simulateDataModification(data);
            modifiedData.getPersons().addAll(simulateDataAddition());

            XmlFileHelper.saveDataToFile(cloudFile, modifiedData.getPersons(), modifiedData.getGroups());
            TimeUnit.SECONDS.sleep(RANDOM_GENERATOR.nextInt(DELAY_RANGE) + MIN_DELAY_IN_SEC);

            return Optional.of(modifiedData);

        } catch (JAXBException e) {
            e.printStackTrace();
            System.out.println("File not found or is not in valid xml format : " + cloudFile);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Optional.empty();
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

    private List<Person> simulateDataAddition() {
        List<Person> newData = new ArrayList<>();

        for (int i = 0; i < MAX_NUM_PERSONS_TO_ADD; i++) {
            if (RANDOM_GENERATOR.nextDouble() <= ADD_PERSON_PROBABILITY) {
                Person person = new Person(java.util.UUID.randomUUID().toString(),
                                           java.util.UUID.randomUUID().toString());
                System.out.println("Cloud simulator: adding " + person);
                newData.add(person);
            }
        }

        return newData;
    }

    /**
     * WARNING: MUTATES data ARGUMENT
     * TODO: currently only modifies Persons
     *
     * @param data
     * @return the (possibly) modified argument addressbookwrapper
     */
    private AddressBook simulateDataModification(AddressBook data) {
        List<Person> modifiedData = new ArrayList<>();

        // currently only modifies persons
        for (Person person : data.getPersons()) {
            if (RANDOM_GENERATOR.nextDouble() <= MODIFY_PERSON_PROBABILITY) {
                System.out.println("Cloud simulator: modifying " + person);
                person.setCity(java.util.UUID.randomUUID().toString());
                person.setStreet(java.util.UUID.randomUUID().toString());
                person.setPostalCode(Integer.toString(RANDOM_GENERATOR.nextInt(999999)));
            }
            modifiedData.add(person);
        }

        data.setPersons(modifiedData);
        return data;
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

        List<Person> persons = getDataListFromBody(cloudResponse.getBody(), new TypeToken<List<ContactGroup>>(){}.getType());
        RateLimitStatus rateLimitStatus = getRateLimitStatusFromHeader(cloudResponse.getHeaders());
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), rateLimitStatus, persons);
    }

    private <T> T getDataFromBody(InputStream bodyStream, Type type) throws IOException {
        return parseJson(bodyStream, type, null);
    }

    private <T> T getDataListFromBody(InputStream bodyStream, Type listType) throws IOException {
        return parseJson(bodyStream, null, listType);
    }

    private List<Person> getGroupsFromBody(InputStream bodyStream) throws IOException {
        Type dataType = new TypeToken<List<ContactGroup>>(){}.getType();
        return parseJson(bodyStream, ContactGroup.class, dataType);
    }

    private RateLimitStatus getRateLimitStatusFromHeader(InputStream headerStream) throws IOException {
        Type headerType = new TypeToken<HashMap<String, Double>>(){}.getType();
        HashMap<String, Long> headers = parseJson(headerStream, headerType, null);
        return new RateLimitStatus(
                headers.get("X-RateLimit-Limit").intValue(),
                headers.get("X-RateLimit-Remaining").intValue(),
                headers.get("X-RateLimit-Reset"));
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

        List<ContactGroup> groups = getDataListFromBody(cloudResponse.getBody(), new TypeToken<List<ContactGroup>>(){}.getType());
        RateLimitStatus rateLimitStatus = getRateLimitStatusFromHeader(cloudResponse.getHeaders());
        return new ExtractedCloudResponse<>(cloudResponse.getResponseCode(), rateLimitStatus, groups);
    }

    /**
     * TODO: refactor
     * Parse JSON to specified type
     *
     * @param <V>
     * @param stream
     * @param type
     * @param listType
     * @return parsed type
     * @throws IOException
     */
    protected <V> V parseJson(InputStream stream, Type type, Type listType)
            throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                stream, "UTF-8"), 8192);
        if (listType == null)
            try {
                return gson.fromJson(reader, type);
            } catch (JsonParseException jpe) {
                IOException ioe = new IOException(
                        "Parse exception converting JSON to object"); //$NON-NLS-1$
                ioe.initCause(jpe);
                throw ioe;
            } finally {
                try {
                    reader.close();
                } catch (IOException ignored) {
                    // Ignored
                }
            }
        else {
            JsonReader jsonReader = new JsonReader(reader);
            try {
                if (jsonReader.peek() == BEGIN_ARRAY)
                    return gson.fromJson(jsonReader, listType);
                else
                    return gson.fromJson(jsonReader, type);
            } catch (JsonParseException jpe) {
                IOException ioe = new IOException(
                        "Parse exception converting JSON to object"); //$NON-NLS-1$
                ioe.initCause(jpe);
                throw ioe;
            } finally {
                try {
                    jsonReader.close();
                } catch (IOException ignored) {
                    // Ignored
                }
            }
        }
    }

    private Person convertToPerson(CloudPerson cloudPerson) {
        Person person = new Person(cloudPerson.getFirstName(), cloudPerson.getLastName());
        person.setStreet(cloudPerson.getStreet());
        person.setCity(cloudPerson.getCity());
        person.setPostalCode(Integer.valueOf(cloudPerson.getPostalCode()));
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
        CloudPerson returnedPerson = getDataFromBody(response.getBody(), new TypeToken<CloudPerson>(){}.getType());
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
        Person returnedPerson = getDataFromBody(response.getBody(), new TypeToken<Person>(){}.getType());
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
        ContactGroup returnedGroup = getDataFromBody(response.getBody(), new TypeToken<ContactGroup>(){}.getType());
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), returnedGroup);
    }

    /**
     * Updates group on the cloud
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
        ContactGroup returnedGroup = getDataFromBody(response.getBody(), new TypeToken<ContactGroup>(){}.getType());
        return new ExtractedCloudResponse<>(response.getResponseCode(), getRateLimitStatusFromHeader(response.getHeaders()), returnedGroup);
    }

    @Override
    public ExtractedCloudResponse<Void> deleteGroup(String addressBookName, String groupName) {
        return null;
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
    public RateLimitStatus getLimitStatus() {
        return cloud.getRateLimitStatus().getData().get();
    }
}
