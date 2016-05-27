package address.sync;

import address.model.AddressBook;
import address.model.datatypes.ContactGroup;
import address.model.datatypes.Person;
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
    private List<Person> personsList;
    private List<ContactGroup> groupList;
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

    private boolean isWithinQuota(int quotaUsed) {
        return quotaUsed <= rateLimitStatus.getQuotaRemaining();
    }

    private boolean isExisting(String firstName, String lastName) {
        return personsList.stream()
                .filter(person -> person.getFirstName().equals(firstName)
                        && person.getLastName().equals(lastName))
                .findAny()
                .isPresent();
    }

    private boolean isExistingPerson(Person person) {
        String firstName = person.getFirstName();
        String lastName = person.getLastName();
        return isExisting(firstName, lastName);
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
    private Person addPerson(String addressBookName, Person newPerson) {
        String newPersonFirstName = newPerson.getFirstName();
        String newPersonLastName = newPerson.getLastName();
        if (newPersonFirstName == null || newPersonLastName == null) return null;
        if (isExistingPerson(newPerson)) return null;

        personsList.add(newPerson);

        return newPerson;
    }

    private Optional<Person> getPerson(String addressBookName, String firstName, String lastName) {
        return personsList.stream()
                .filter(person -> person.getFirstName().equals(lastName)
                        && person.getLastName().equals(lastName))
                .findAny();
    }

    private Person updatePersonDetails(String addressBookName, String oldFirstName, String oldLastName, Person updatedPerson) {
        Optional<Person> personQueryResult = getPerson(addressBookName, oldFirstName, oldLastName);
        if (!personQueryResult.isPresent()) throw new NoSuchElementException("No such person found.");

        Person oldPerson = personQueryResult.get();
        oldPerson.update(updatedPerson);
        return oldPerson;
    }

    /**
     * Attempts to create a person if quota is available
     * @param addressBookName
     * @param newPerson
     * @return a response wrapper, containing the added person if successful
     */
    public RawCloudResponse createPerson(String addressBookName, Person newPerson) {
        int noOfRequestsRequired = 1;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return new RawCloudResponse(HttpURLConnection.HTTP_FORBIDDEN, null, convertToInputStream(getStandardHeaders()));
        }
        rateLimitStatus.setQuotaRemaining(0);

        Person returnedPerson = addPerson(addressBookName, newPerson);

        if (returnedPerson == null) {
            return new RawCloudResponse(HttpURLConnection.HTTP_BAD_REQUEST, null, convertToInputStream(getStandardHeaders()));
        }
        return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(returnedPerson), convertToInputStream(getStandardHeaders()));
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
    public RawCloudResponse updatePerson(String addressBookName, String oldFirstName, String oldLastName, Person updatedPerson) {
        int noOfRequestsRequired = 1;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return new RawCloudResponse(HttpURLConnection.HTTP_FORBIDDEN, null, convertToInputStream(getStandardHeaders()));
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        Person resultingPerson = updatePersonDetails(addressBookName, oldFirstName, oldLastName, updatedPerson);
        return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(resultingPerson), convertToInputStream(getStandardHeaders()));
    }
}
