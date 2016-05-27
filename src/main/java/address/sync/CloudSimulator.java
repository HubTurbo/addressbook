package address.sync;

import address.model.AddressBook;
import address.model.datatypes.ContactGroup;
import address.model.datatypes.Person;
import address.sync.model.CloudAddressBook;
import address.sync.model.CloudGroup;
import address.sync.model.CloudPerson;
import address.util.XmlFileHelper;
import com.google.gson.Gson;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class CloudSimulator implements ICloudSimulator {
    private static final int API_QUOTA_PER_HOUR = 5000;

    private static final Random RANDOM_GENERATOR = new Random();
    private static final double FAILURE_PROBABILITY = 0.1;

    private static final int MIN_DELAY_IN_SEC = 1;
    private static final int DELAY_RANGE = 5;

    private static final double MODIFY_PERSON_PROBABILITY = 0.1;
    private static final double ADD_PERSON_PROBABILITY = 0.05;
    private static final int MAX_NUM_PERSONS_TO_ADD = 2;
    RateLimitStatus rateLimitStatus;
    private List<CloudPerson> personsList;
    private List<CloudGroup> groupList;
    private boolean hasResetCurrentQuota;
    private boolean shouldSimulateUnreliableNetwork;
    private Gson gson;

    CloudSimulator(boolean shouldSimulateUnreliableNetwork) {
        personsList = new ArrayList<>();
        groupList = new ArrayList<>();
        rateLimitStatus = new RateLimitStatus(API_QUOTA_PER_HOUR, API_QUOTA_PER_HOUR, getNextResetTime());
        hasResetCurrentQuota = true;
        this.shouldSimulateUnreliableNetwork = shouldSimulateUnreliableNetwork;
        gson = new Gson();

        File cloudFile = new File(".$TEMP_ADDRESS_BOOK_MIRROR");

        if (cloudFile.exists()) {
            System.out.println("Reading from cloudFile: " + cloudFile.canRead());
            try {
                CloudAddressBook data = XmlFileHelper.getCloudDataFromFile(cloudFile);
                personsList.addAll(data.getAllPersons());
                groupList.addAll(data.getAllGroups());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        } else {
            try {
                cloudFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Attempts to create a person if quota is available
     * @param addressBookName
     * @param newPerson
     * @return a response wrapper, containing the added person if successful
     */
    public RawCloudResponse createPerson(String addressBookName, CloudPerson newPerson) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        int noOfRequestsRequired = 1;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return new RawCloudResponse(HttpURLConnection.HTTP_FORBIDDEN, null, convertToInputStream(getStandardHeaders()));
        }
        rateLimitStatus.setQuotaRemaining(0);

        try {
            CloudPerson returnedPerson = addPerson(addressBookName, newPerson);
            return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(returnedPerson), convertToInputStream(getStandardHeaders()));
        } catch (IllegalArgumentException e) {
            return new RawCloudResponse(HttpURLConnection.HTTP_BAD_REQUEST, null, convertToInputStream(getStandardHeaders()));
        }
    }

    /**
     * Returns a response wrapper containing the list of persons if quota is available
     * @param addressBookName
     * @param resourcesPerPage
     * @return
     */
    public RawCloudResponse getPersons(String addressBookName, int resourcesPerPage) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        int noOfRequestsRequired = getNumberOfRequestsRequired(personsList.size(), resourcesPerPage);
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return new RawCloudResponse(HttpURLConnection.HTTP_FORBIDDEN, null, convertToInputStream(getStandardHeaders()));
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(personsList), convertToInputStream(getStandardHeaders()));
    }

    /**
     * Returns a response wrapper containing the list of groups if quota is available
     * @param addressBookName
     * @param resourcesPerPage
     * @return
     */
    public RawCloudResponse getGroups(String addressBookName, int resourcesPerPage) {
        int noOfRequestsRequired = getNumberOfRequestsRequired(groupList.size(), resourcesPerPage);
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return new RawCloudResponse(HttpURLConnection.HTTP_FORBIDDEN, null, convertToInputStream(getStandardHeaders()));
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(groupList), convertToInputStream(getStandardHeaders()));
    }

    /**
     * Gets the rate limit given, rate limit remaining, and the time the rate limit quota is reset
     * @return
     */
    public RawCloudResponse getRateLimitStatus() {
        return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(rateLimitStatus), convertToInputStream(getStandardHeaders()));
    }

    /**
     * Updates the details of the person with details of the updatedPerson if quota is available
     *
     * @param addressBookName
     * @param oldFirstName
     * @param oldLastName
     * @param updatedPerson
     * @return
     */
    public RawCloudResponse updatePerson(String addressBookName, String oldFirstName, String oldLastName, CloudPerson updatedPerson) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        int noOfRequestsRequired = 1;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return new RawCloudResponse(HttpURLConnection.HTTP_FORBIDDEN, null, convertToInputStream(getStandardHeaders()));
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        try {
            CloudPerson resultingPerson = updatePersonDetails(addressBookName, oldFirstName, oldLastName, updatedPerson);
            return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(resultingPerson), convertToInputStream(getStandardHeaders()));
        } catch (NoSuchElementException e) {
            return new RawCloudResponse(HttpURLConnection.HTTP_BAD_REQUEST, null, convertToInputStream(getStandardHeaders()));
        }
    }

    /**
     * Deletes the person uniquely identified by addressBookName, firstName and lastName, if quota is available
     *
     * @param addressBookName
     * @param firstName
     * @param lastName
     * @return
     */
    public RawCloudResponse deletePerson(String addressBookName, String firstName, String lastName) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        int noOfRequestsRequired = 1;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return new RawCloudResponse(HttpURLConnection.HTTP_FORBIDDEN, null, convertToInputStream(getStandardHeaders()));
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        try {
            deletePersonFromData(addressBookName, firstName, lastName);
            return new RawCloudResponse(HttpURLConnection.HTTP_NO_CONTENT, null, convertToInputStream(getStandardHeaders()));
        } catch (NoSuchElementException e) {
            return new RawCloudResponse(HttpURLConnection.HTTP_BAD_REQUEST, null, convertToInputStream(getStandardHeaders()));
        }
    }

    /**
     * Creates a new group, if quota is available
     * @param addressBookName
     * @param newGroup group name should not already be used
     * @return
     */
    public RawCloudResponse createGroup(String addressBookName, CloudGroup newGroup) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        int noOfRequestsRequired = 1;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return new RawCloudResponse(HttpURLConnection.HTTP_FORBIDDEN, null, convertToInputStream(getStandardHeaders()));
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        try {
            CloudGroup returnedGroup = addGroup(addressBookName, newGroup);
            return new RawCloudResponse(HttpURLConnection.HTTP_CREATED, convertToInputStream(returnedGroup), convertToInputStream(getStandardHeaders()));
        } catch (IllegalArgumentException e) {
            return new RawCloudResponse(HttpURLConnection.HTTP_BAD_REQUEST, null, convertToInputStream(getStandardHeaders()));
        }
    }

    /**
     * Updates details of a group to details of updatedGroup, if quota is available
     * @param addressBookName
     * @param oldGroupName
     * @param updatedGroup
     * @return
     */
    public RawCloudResponse editGroup(String addressBookName, String oldGroupName, CloudGroup updatedGroup) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        int noOfRequestsRequired = 1;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return new RawCloudResponse(HttpURLConnection.HTTP_FORBIDDEN, null, convertToInputStream(getStandardHeaders()));
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        try {
            CloudGroup returnedGroup = updateGroupDetails(addressBookName, oldGroupName, updatedGroup);
            return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(returnedGroup), convertToInputStream(getStandardHeaders()));
        } catch (NoSuchElementException e) {
            return new RawCloudResponse(HttpURLConnection.HTTP_BAD_REQUEST, null, convertToInputStream(getStandardHeaders()));
        }
    }

    /**
     * Deletes a group uniquely identified by its name, if quota is available
     * @param addressBookName
     * @param groupName
     * @return
     */
    public RawCloudResponse deleteGroup(String addressBookName, String groupName) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        int noOfRequestsRequired = 1;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return new RawCloudResponse(HttpURLConnection.HTTP_FORBIDDEN, null, convertToInputStream(getStandardHeaders()));
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        try {
            deleteGroupFromData(addressBookName, groupName);
            return new RawCloudResponse(HttpURLConnection.HTTP_NO_CONTENT, null, convertToInputStream(getStandardHeaders()));
        } catch (NoSuchElementException e) {
            return new RawCloudResponse(HttpURLConnection.HTTP_BAD_REQUEST, null, convertToInputStream(getStandardHeaders()));
        }
    }

    private long getNextResetTime() {
        LocalDateTime curTime = LocalDateTime.now();
        LocalDateTime nearestHour = LocalDateTime.of(
                curTime.getYear(), curTime.getMonth(), curTime.getDayOfMonth(), curTime.getHour() + 1,
                0, curTime.getSecond(), curTime.getNano());
        return nearestHour.toEpochSecond(ZoneOffset.of("Z"));
    }

    private boolean shouldSimulateNetworkFailure() {
        return shouldSimulateUnreliableNetwork && RANDOM_GENERATOR.nextDouble() <= FAILURE_PROBABILITY;
    }

    private boolean shouldSimulateSlowResponse() {
        return shouldSimulateUnreliableNetwork && RANDOM_GENERATOR.nextDouble() <= FAILURE_PROBABILITY;
    }

    private RawCloudResponse getNetworkFailedResponse() {
        System.out.println("Cloud simulator: failure occurred! Could not retrieve data");
        return new RawCloudResponse(HttpURLConnection.HTTP_INTERNAL_ERROR, null, null);
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
                person.setPostalCode(RANDOM_GENERATOR.nextInt(999999));
            }
            modifiedData.add(person);
        }

        data.setPersons(modifiedData);
        return data;
    }

    private boolean isWithinQuota(int quotaUsed) {
        return quotaUsed <= rateLimitStatus.getQuotaRemaining();
    }

    private boolean isExistingPerson(CloudPerson targetPerson) {
        return personsList.stream()
                .filter(person -> person.getFirstName().equals(targetPerson.getFirstName())
                        && person.getLastName().equals(targetPerson.getLastName()))
                .findAny()
                .isPresent();
    }

    private boolean isExistingGroup(CloudGroup targetGroup) {
        return groupList.stream()
                .filter(group -> !targetGroup.getName().equals(targetGroup.getName()))
                .findAny()
                .isPresent();
    }

    private HashMap<String, String> getStandardHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("X-RateLimit-Limit", String.valueOf(rateLimitStatus.getQuotaLimit()));
        headers.put("X-RateLimit-Remaining", String.valueOf(rateLimitStatus.getQuotaRemaining()));
        headers.put("X-RateLimit-Reset", String.valueOf(rateLimitStatus.getQuotaReset()));
        return headers;
    }

    /**
     * Verifies whether newPerson can be added, and adds it to the persons list
     *
     * @param addressBookName
     * @param newPerson
     * @return newPerson, if added, else null
     */
    private CloudPerson addPerson(String addressBookName, CloudPerson newPerson) {
        if (newPerson == null) throw new IllegalArgumentException("Person cannot be null");
        String newPersonFirstName = newPerson.getFirstName();
        String newPersonLastName = newPerson.getLastName();
        if (newPersonFirstName == null || newPersonLastName == null) throw new IllegalArgumentException("Fields cannot be null");
        if (isExistingPerson(newPerson)) throw new IllegalArgumentException("Person already exists");

        personsList.add(newPerson);

        return newPerson;
    }

    private Optional<CloudPerson> getPerson(String addressBookName, String firstName, String lastName) {
        return personsList.stream()
                .filter(person -> person.getFirstName().equals(firstName)
                        && person.getLastName().equals(lastName))
                .findAny();
    }

    private CloudPerson updatePersonDetails(String addressBookName, String oldFirstName, String oldLastName, CloudPerson updatedPerson) throws NoSuchElementException {
        CloudPerson oldPerson = getPersonIfExists(addressBookName, oldFirstName, oldLastName);
        oldPerson.updatedBy(updatedPerson);
        return oldPerson;
    }

    private CloudPerson getPersonIfExists(String addressBookName, String oldFirstName, String oldLastName) {
        Optional<CloudPerson> personQueryResult = getPerson(addressBookName, oldFirstName, oldLastName);
        if (!personQueryResult.isPresent()) throw new NoSuchElementException("No such person found.");

        return personQueryResult.get();
    }

    private void delayRandomAmount() {
        long delayAmount = RANDOM_GENERATOR.nextInt(DELAY_RANGE) + MIN_DELAY_IN_SEC;
        try {
            TimeUnit.SECONDS.sleep(delayAmount);
        } catch (InterruptedException e) {
            System.out.println("Error occurred while adding cloud response delay.");
        }
    }

    private ByteArrayInputStream convertToInputStream(Object object) {
        return new ByteArrayInputStream(gson.toJson(object).getBytes());
    }

    private int getNumberOfRequestsRequired(int dataSize, int resourcesPerPage) {
        return (int) Math.ceil((double)dataSize / resourcesPerPage);
    }

    private void deletePersonFromData(String addressBookName, String firstName, String lastName) throws NoSuchElementException {
        CloudPerson deletedPerson = getPersonIfExists(addressBookName, firstName, lastName);
        deletedPerson.setDeleted(true);
    }

    private CloudGroup addGroup(String addressBookName, CloudGroup newGroup) {
        if (newGroup == null) throw new IllegalArgumentException("Group cannot be null");
        String groupName = newGroup.getName();
        if (groupName == null) throw new IllegalArgumentException("Fields cannot be null");
        if (isExistingGroup(newGroup)) throw new IllegalArgumentException("Group already exists");
        groupList.add(newGroup);
        return newGroup;
    }

    private Optional<CloudGroup> getGroup(String addressBookName, String groupName) {
        return groupList.stream()
                .filter(group -> group.getName().equals(groupName))
                .findAny();
    }

    private CloudGroup getGroupIfExists(String addressBookName, String groupName) {
        Optional<CloudGroup> groupQueryResult = getGroup(addressBookName, groupName);
        if (!groupQueryResult.isPresent()) throw new NoSuchElementException("No such group found.");

        return groupQueryResult.get();
    }

    private CloudGroup updateGroupDetails(String addressBookName, String oldGroupName, CloudGroup updatedGroup) throws NoSuchElementException {
        CloudGroup oldGroup = getGroupIfExists(addressBookName, oldGroupName);
        oldGroup.updatedBy(updatedGroup);
        return oldGroup;
    }

    private void deleteGroupFromData(String addressBookName, String groupName) throws NoSuchElementException {
        CloudGroup group = getGroupIfExists(addressBookName, groupName);
        // This may differ from how GitHub does it, but we won't know for sure
        groupList.remove(group);
    }
}
