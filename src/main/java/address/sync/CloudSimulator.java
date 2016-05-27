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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class CloudSimulator {
    private List<CloudPerson> personsList;
    private List<CloudGroup> groupList;
    RateLimitStatus rateLimitStatus;
    private boolean hasResetCurrentQuota;
    private Gson gson;
    private static final int API_QUOTA_PER_HOUR = 5000;

    private long getNextResetTime() {
        LocalDateTime curTime = LocalDateTime.now();
        LocalDateTime nearestHour = LocalDateTime.of(
                curTime.getYear(), curTime.getMonth(), curTime.getDayOfMonth(), curTime.getHour() + 1,
                0, curTime.getSecond(), curTime.getNano());
        return nearestHour.toEpochSecond(ZoneOffset.of("GMT"));
    }

    CloudSimulator() {
        personsList = new ArrayList<>();
        groupList = new ArrayList<>();
        rateLimitStatus = new RateLimitStatus(API_QUOTA_PER_HOUR, API_QUOTA_PER_HOUR, getNextResetTime());
        hasResetCurrentQuota = true;
        gson = new Gson();

        File cloudFile = new File(".$TEMP_ADDRESS_BOOK_MIRROR");
        System.out.println("Reading from cloudFile: " + cloudFile.canRead());
        try {
            CloudAddressBook data = XmlFileHelper.getCloudDataFromFile(cloudFile);
            personsList.addAll(data.getAllPersons());
            groupList.addAll(data.getAllGroups());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
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

    /**
     * Attempts to create a person if quota is available
     * @param addressBookName
     * @param newPerson
     * @return a response wrapper, containing the added person if successful
     */
    public RawCloudResponse createPerson(String addressBookName, CloudPerson newPerson) {
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

    private ByteArrayInputStream convertToInputStream(Object object) {
        return new ByteArrayInputStream(gson.toJson(object).getBytes());
    }


    /**
     * Returns a response wrapper containing the list of persons if quota is available
     * @param addressBookName
     * @param resourcesPerPage
     * @return
     */
    public RawCloudResponse getPersons(String addressBookName, int resourcesPerPage) {
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

        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return new RawCloudResponse(HttpURLConnection.HTTP_FORBIDDEN, null, convertToInputStream(getStandardHeaders()));
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(groupList), convertToInputStream(getStandardHeaders()));
    }

    public ExtractedCloudResponse<RateLimitStatus> getRateLimitStatus() {
        return new ExtractedCloudResponse<>(HttpURLConnection.HTTP_OK, rateLimitStatus, rateLimitStatus);
    }

    private int getNumberOfRequestsRequired(int dataSize, int resourcesPerPage) {
        return (int) Math.ceil((double)dataSize / resourcesPerPage);
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

    private void deletePersonFromData(String addressBookName, String firstName, String lastName) throws NoSuchElementException {
        CloudPerson deletedPerson = getPersonIfExists(addressBookName, firstName, lastName);
        deletedPerson.setDeleted(true);
    }

    /**
     * Deletes the person uniquely identified by addressBookName, firstName and lastName
     *
     * @param addressBookName
     * @param firstName
     * @param lastName
     * @return
     */
    public RawCloudResponse deletePerson(String addressBookName, String firstName, String lastName) {
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

    private CloudGroup addGroup(String addressBookName, CloudGroup newGroup) {
        if (newGroup == null) throw new IllegalArgumentException("Group cannot be null");
        String groupName = newGroup.getName();
        if (groupName == null) throw new IllegalArgumentException("Fields cannot be null");
        if (isExistingGroup(newGroup)) throw new IllegalArgumentException("Group already exists");
        groupList.add(newGroup);
        return newGroup;
    }

    public RawCloudResponse createGroup(String addressBookName, CloudGroup newGroup) {
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

    public RawCloudResponse editGroup(String addressBookName, String oldGroupName, CloudGroup updatedGroup) {
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
}
