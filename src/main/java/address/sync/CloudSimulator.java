package address.sync;

import address.model.AddressBook;
import address.model.datatypes.ContactGroup;
import address.model.datatypes.Person;
import address.util.XmlFileHelper;
import com.google.gson.Gson;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

// TODO implement full range of possible unreliable network effects: fail, corruption, etc
public class CloudSimulator implements ICloudSimulator {
    private static final double FAILURE_PROBABILITY = 0.1;

    private static final int MIN_DELAY_IN_SEC = 1;
    private static final int DELAY_RANGE = 5;

    private static final double MODIFY_PERSON_PROBABILITY = 0.1;
    private static final double ADD_PERSON_PROBABILITY = 0.05;
    private static final int MAX_NUM_PERSONS_TO_ADD = 2;
    private static final Random RANDOM_GENERATOR = new Random();
    private static final int API_QUOTA_PER_HOUR = 5000;
    private static final int RESOURCES_PER_PAGE = 100;

    private Gson gson;
    private boolean simulateUnreliableNetwork;
    private int quotaRemaining;
    private int quotaLimit;
    private long quotaReset;
    private boolean hasResetCurrentQuota;

    private List<Person> personsList;
    private List<ContactGroup> groupList;

    public CloudSimulator(boolean shouldSimulateUnreliableNetwork) {
        gson = new Gson();
        simulateUnreliableNetwork = shouldSimulateUnreliableNetwork;
        quotaLimit = API_QUOTA_PER_HOUR;
        quotaRemaining = API_QUOTA_PER_HOUR;
        quotaReset = getNextResetTime();
        hasResetCurrentQuota = true;

        personsList = new ArrayList<>();
        groupList = new ArrayList<>();

        File cloudFile = new File(".$TEMP_ADDRESS_BOOK");
        System.out.println("Reading from cloudFile: " + cloudFile.canRead());
        try {
            AddressBook data = XmlFileHelper.getDataFromFile(cloudFile);
            System.out.println("data.getPersons size: " + data.getPersons().size());
            personsList.addAll(data.getPersons());
            groupList.addAll(data.getGroups());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private long getNextResetTime() {
        LocalDateTime curTime = LocalDateTime.now();
        LocalDateTime nearestHour = LocalDateTime.of(
                curTime.getYear(), curTime.getMonth(), curTime.getDayOfMonth(), curTime.getHour() + 1,
                0, curTime.getSecond(), curTime.getNano());
        return nearestHour.toEpochSecond(ZoneOffset.of("GMT"));
    }

    public static void main(String[] args) {
        CloudSimulator test = new CloudSimulator(false);
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
     */
    public void requestChangesToCloud(File file, List<Person> people, List<ContactGroup> groups)
            throws JAXBException {
        if (file == null) return;
        XmlFileHelper.saveDataToFile(file, people, groups);
        try {
            TimeUnit.SECONDS.sleep(RANDOM_GENERATOR.nextInt(5) + MIN_DELAY_IN_SEC);
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
                person.setPostalCode(java.util.UUID.randomUUID().toString());
            }
            modifiedData.add(person);
        }

        data.setPersons(modifiedData);
        return data;
    }

    /**
     * Deducts apiUseCount from the remaining quota
     *
     * apiUseCount should not be bigger than useQuota
     *
     * @param apiUseCount
     */
    private void useQuota(int apiUseCount) {
        quotaRemaining -= apiUseCount;
        assert quotaRemaining >= 0 : "quotaRemaining cannot be negative.";
    }

    private void useUpQuota() {
        quotaRemaining = 0;
    }

    private boolean isWithinQuota(int quotaUsed) {
        return quotaUsed <= quotaRemaining;
    }

    @Override
    public Optional<CloudResponse<List<Person>>> getPersons(String addressBookName) {
        List<Person> dataRetrievedFromCloud = personsList;
        int noOfRequestsRequired = getNumberOfRequestsRequired(dataRetrievedFromCloud.size());

        if (!isWithinQuota(noOfRequestsRequired)) {
            useUpQuota();
            return Optional.empty();
        }
        useQuota(noOfRequestsRequired);

        CloudResponse<List<Person>> response = new CloudResponse<>();
        response.setData(personsList);
        response.setQuotaLimit(quotaLimit);
        response.setQuotaRemaining(quotaRemaining);
        response.setQuotaReset(quotaReset);

        return Optional.of(response);
    }

    private int getNumberOfRequestsRequired(int dataSize) {
        return (int) Math.ceil((double)dataSize / RESOURCES_PER_PAGE);
    }

    @Override
    public Optional<CloudResponse<List<ContactGroup>>> getGroups(String addressBookName) {
        List<ContactGroup> dataRetrievedFromCloud = groupList;
        int noOfRequestsRequired = getNumberOfRequestsRequired(dataRetrievedFromCloud.size());

        if (!isWithinQuota(noOfRequestsRequired)) {
            useUpQuota();
            return Optional.empty();
        }
        useQuota(noOfRequestsRequired);

        CloudResponse<List<ContactGroup>> response = new CloudResponse<>();
        response.setData(groupList);
        response.setQuotaLimit(quotaLimit);
        response.setQuotaRemaining(quotaRemaining);
        response.setQuotaReset(quotaReset);
        return Optional.of(response);
    }

    @Override
    public Optional<CloudResponse<Person>> createPerson(String addressBookName, Person person) {
        return null;
    }

    @Override
    public Optional<CloudResponse<Person>> updatePerson(String addressBookName, String oldFirstName, String oldLastName, Person updatedPerson) {
        return null;
    }

    @Override
    public Optional<Boolean> deletePerson(String addressBookName, int personId) {
        return null;
    }

    @Override
    public Optional<CloudResponse> createGroup(String addressBookName, ContactGroup group) {
        return null;
    }

    @Override
    public Optional<CloudResponse> editGroup(String addressBookName, String oldGroupName, ContactGroup newGroup) {
        return null;
    }

    @Override
    public Optional<Boolean> deleteGroup(String addressBookName, String groupName) {
        return null;
    }

    @Override
    public Optional<CloudResponse> getUpdatedPersons(String addressBookName) {
        return null;
    }

    @Override
    public Optional<CloudResponse> getUpdatedGroups(String addressBookName) {
        return null;
    }

    @Override
    public int getQuotaLimit() {
        return quotaLimit;
    }

    @Override
    public int getQuotaRemaining() {
        return quotaRemaining;
    }

    @Override
    public long getQuotaReset() {
        return quotaReset;
    }
}
