package address.sync;

import address.model.AddressBook;
import address.model.datatypes.Person;
import address.sync.model.CloudAddressBook;
import address.sync.model.CloudTag;
import address.sync.model.CloudPerson;
import address.util.JsonUtil;
import address.util.TickingTimer;
import address.util.XmlFileHelper;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// TODO implement full range of possible unreliable network effects: fail, corruption, etc
public class CloudSimulator implements ICloudSimulator {
    private static final int API_QUOTA_PER_HOUR = 5000;

    private static final Random RANDOM_GENERATOR = new Random();
    private static final double FAILURE_PROBABILITY = 0.1;

    private static final int MIN_DELAY_IN_SEC = 1;
    private static final int DELAY_RANGE = 5;

    private static final double MODIFY_PERSON_PROBABILITY = 0.1;
    private static final double ADD_PERSON_PROBABILITY = 0.05;
    private static final int MAX_NUM_PERSONS_TO_ADD = 2;

    private RateLimitStatus rateLimitStatus;
    private boolean shouldSimulateUnreliableNetwork;
    private TickingTimer timer;

    CloudSimulator(boolean shouldSimulateUnreliableNetwork) {
        rateLimitStatus = new RateLimitStatus(API_QUOTA_PER_HOUR, API_QUOTA_PER_HOUR, getNextResetTime());
        resetQuotaAndRestartTimer();
        this.shouldSimulateUnreliableNetwork = shouldSimulateUnreliableNetwork;
    }

    private CloudAddressBook readCloudAddressBookFromFile(String addressBookName) throws JAXBException {
        File cloudFile = getCloudDataFilePath(addressBookName);
        System.out.println("Reading from cloudFile: " + cloudFile.canRead());
        try {
            CloudAddressBook data = XmlFileHelper.getCloudDataFromFile(cloudFile);
            return data;
        } catch (JAXBException e) {
            System.out.println("Error reading from cloud file.");
            throw e;
        }
    }

    private File getCloudDataFilePath(String addressBookName) {
        return new File("/cloud/" + addressBookName);
    }

    private void writeCloudAddressBookToFile(String addressBookName, CloudAddressBook cloudAddressBook) throws JAXBException {
        File cloudFile = getCloudDataFilePath(addressBookName);
        System.out.println("Writing to cloudFile: " + cloudFile.canRead());
        try {
            XmlFileHelper.saveCloudDataToFile(cloudFile, cloudAddressBook);
        } catch (JAXBException e) {
            System.out.println("Error writing to cloud file.");
            throw e;
        }
    }

    /**
     * Attempts to create a person if quota is available
     *
     * Consumes 1 API usage
     *
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
            return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);

        try {
            CloudAddressBook fileData = readCloudAddressBookFromFile(addressBookName);
            CloudPerson returnedPerson = addPerson(fileData.getAllPersons(), newPerson);
            writeCloudAddressBookToFile(addressBookName, fileData);
            return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(returnedPerson), convertToInputStream(getStandardHeaders()));
        } catch (IllegalArgumentException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (JAXBException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    private RawCloudResponse getEmptyResponse(int responseCode) {
        return new RawCloudResponse(responseCode, null, convertToInputStream(getStandardHeaders()));
    }

    /**
     * Returns a response wrapper containing the list of persons if quota is available
     *
     * Consumes 1 + floor(persons size/resourcesPerPage) API usage
     *
     * @param addressBookName
     * @param resourcesPerPage
     * @return
     */
    public RawCloudResponse getPersons(String addressBookName, int resourcesPerPage) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        List<CloudPerson> personList;
        try {
            CloudAddressBook fileData = readCloudAddressBookFromFile(addressBookName);
            personList = fileData.getAllPersons();
        } catch (JAXBException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        int noOfRequestsRequired = 1 + getNumberOfRequestsRequired(personList.size(), resourcesPerPage);
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(getIntactPersonsList(personList)), convertToInputStream(getStandardHeaders()));
    }

    /**
     * Returns a response wrapper containing the list of tags if quota is available
     *
     * Consumes 1 + floor(tag list/resourcesPerPage) API usage
     *
     * @param addressBookName
     * @param resourcesPerPage
     * @return
     */
    public RawCloudResponse getTags(String addressBookName, int resourcesPerPage) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        List<CloudTag> tagList;

        try {
            CloudAddressBook fileData = readCloudAddressBookFromFile(addressBookName);
            tagList = fileData.getAllTags();
        } catch (JAXBException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        int noOfRequestsRequired = 1 + getNumberOfRequestsRequired(tagList.size(), resourcesPerPage);
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(tagList), convertToInputStream(getStandardHeaders()));
    }

    /**
     * Gets the rate limit allocated, quota remaining, and the time the given quota is reset
     *
     * This does NOT cost any API usage
     *
     * @return
     */
    public RawCloudResponse getRateLimitStatus() {
        return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(rateLimitStatus), convertToInputStream(getStandardHeaders()));
    }

    /**
     * Updates the details of the person with details of the updatedPerson if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param oldFirstName
     * @param oldLastName
     * @param updatedPerson
     * @return
     */
    public RawCloudResponse updatePerson(String addressBookName, String oldFirstName, String oldLastName,
                                         CloudPerson updatedPerson) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        int noOfRequestsRequired = 1;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        try {
            CloudAddressBook fileData = readCloudAddressBookFromFile(addressBookName);
            CloudPerson resultingPerson = updatePersonDetails(fileData.getAllPersons(), oldFirstName, oldLastName,
                                                              updatedPerson);
            writeCloudAddressBookToFile(addressBookName, fileData);
            return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(resultingPerson),
                                        convertToInputStream(getStandardHeaders()));
        } catch (NoSuchElementException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (JAXBException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Deletes the person uniquely identified by addressBookName, firstName and lastName, if quota is available
     *
     * Consumes 1 API usage
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
            return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        try {
            CloudAddressBook fileData = readCloudAddressBookFromFile(addressBookName);
            deletePersonFromData(fileData.getAllPersons(), firstName, lastName);
            writeCloudAddressBookToFile(addressBookName, fileData);
            return new RawCloudResponse(HttpURLConnection.HTTP_NO_CONTENT, null,
                                        convertToInputStream(getStandardHeaders()));
        } catch (NoSuchElementException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (JAXBException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Creates a new tag, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param newTag          tag name should not already be used
     * @return
     */
    public RawCloudResponse createTag(String addressBookName, CloudTag newTag) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        int noOfRequestsRequired = 1;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        try {
            CloudAddressBook fileData = readCloudAddressBookFromFile(addressBookName);
            CloudTag returnedTag = addTag(fileData.getAllTags(), newTag);
            writeCloudAddressBookToFile(addressBookName, fileData);
            return new RawCloudResponse(HttpURLConnection.HTTP_CREATED, convertToInputStream(returnedTag),
                                        convertToInputStream(getStandardHeaders()));
        } catch (IllegalArgumentException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (JAXBException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Updates details of a tag to details of updatedTag, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param oldTagName
     * @param updatedTag
     * @return
     */
    public RawCloudResponse editTag(String addressBookName, String oldTagName, CloudTag updatedTag) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        int noOfRequestsRequired = 1;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        try {
            CloudAddressBook fileData = readCloudAddressBookFromFile(addressBookName);
            CloudTag returnedTag = updateTagDetails(fileData.getAllTags(), oldTagName, updatedTag);
            writeCloudAddressBookToFile(addressBookName, fileData);
            return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(returnedTag),
                                        convertToInputStream(getStandardHeaders()));
        } catch (NoSuchElementException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (JAXBException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Deletes a tag uniquely identified by its name, if quota is available
     *
     * Consumes 1 API usage
     *
     * @param addressBookName
     * @param tagName
     * @return
     */
    public RawCloudResponse deleteTag(String addressBookName, String tagName) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        int noOfRequestsRequired = 1;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        try {
            CloudAddressBook fileData = readCloudAddressBookFromFile(addressBookName);
            deleteTagFromData(fileData.getAllTags(), tagName);
            writeCloudAddressBookToFile(addressBookName, fileData);
            return getEmptyResponse(HttpURLConnection.HTTP_NO_CONTENT);
        } catch (NoSuchElementException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        } catch (JAXBException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Gets the list of persons that have been updated after a certain time
     *
     * Consumes 1 + floor(updated person list/resourcesPerPage) API usage
     *
     * @param addressBookName
     * @param timeString
     * @return
     */
    public RawCloudResponse getUpdatedPersons(String addressBookName, String timeString, int resourcesPerPage) {
        if (shouldSimulateNetworkFailure()) return getNetworkFailedResponse();
        if (shouldSimulateSlowResponse()) delayRandomAmount();

        List<CloudPerson> personList;
        try {
            CloudAddressBook fileData = readCloudAddressBookFromFile(addressBookName);
            personList = fileData.getAllPersons();
        } catch (JAXBException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        int noOfRequestsRequired = 1 + getNumberOfRequestsRequired(personList.size(), resourcesPerPage);;
        if (!isWithinQuota(noOfRequestsRequired)) {
            rateLimitStatus.setQuotaRemaining(0);
            return getEmptyResponse(HttpURLConnection.HTTP_FORBIDDEN);
        }
        rateLimitStatus.useQuota(noOfRequestsRequired);
        try {
            LocalDateTime time = LocalDateTime.parse(timeString);
            List<CloudPerson> resultList = filterPersonsByTime(personList, time);
            return new RawCloudResponse(HttpURLConnection.HTTP_OK, convertToInputStream(resultList),
                                        convertToInputStream(getStandardHeaders()));
        } catch (NoSuchElementException e) {
            return getEmptyResponse(HttpURLConnection.HTTP_BAD_REQUEST);
        }
    }

    private void resetQuotaAndRestartTimer() {
        long nextResetTime = getNextResetTime();
        rateLimitStatus.setQuotaResetTime(nextResetTime);
        rateLimitStatus.setQuotaRemaining(API_QUOTA_PER_HOUR);
        if (timer != null) {
            timer.stop();
        }
        int timeout = (int) (rateLimitStatus.getQuotaReset() - LocalDateTime.now().toEpochSecond(getSystemTimezone()));
        timer = new TickingTimer("Cloud Quota Reset Time", timeout, this::printTimeLeft,
                                this::resetQuotaAndRestartTimer, TimeUnit.SECONDS);
        timer.start();
    }

    private void printTimeLeft(int timeLeft) {
        System.out.println(timeLeft + " seconds remaining to quota reset.");
    }

    private List<CloudPerson> getIntactPersonsList(List<CloudPerson> personsList) {
        return personsList.stream()
                .filter(CloudPerson::isDeleted)
                .collect(Collectors.toList());
    }

    private List<CloudPerson> filterPersonsByTime(List<CloudPerson> personList, LocalDateTime time) {
        return personList.stream()
                .filter(person -> !person.getLastUpdatedAt().isBefore(time))
                .collect(Collectors.toList());
    }

    private long getNextResetTime() {
        LocalDateTime curTime = LocalDateTime.now();
        LocalDateTime nearestHour = LocalDateTime.of(
                curTime.getYear(), curTime.getMonth(), curTime.getDayOfMonth(), curTime.getHour() + 1,
                0, 0, 0);

        return nearestHour.toEpochSecond(getSystemTimezone());
    }

    private ZoneOffset getSystemTimezone() {
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneOffset.systemDefault());
        return zonedDateTime.getOffset();
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

    @Deprecated
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
    @Deprecated
    private AddressBook simulateDataModification(AddressBook data) {
        List<Person> modifiedData = new ArrayList<>();

        // currently only modifies persons
        for (Person person : data.getPersons()) {
            if (RANDOM_GENERATOR.nextDouble() <= MODIFY_PERSON_PROBABILITY) {
                System.out.println("Cloud simulator: modifying " + person);
                person.setCity(java.util.UUID.randomUUID().toString());
                person.setStreet(java.util.UUID.randomUUID().toString());
                person.setPostalCode(String.valueOf(RANDOM_GENERATOR.nextInt(999999)));
            }
            modifiedData.add(person);
        }

        data.setPersons(modifiedData);
        return data;
    }

    private boolean isWithinQuota(int quotaUsed) {
        System.out.println("Current quota left: " + rateLimitStatus.getQuotaRemaining() + ", using " + quotaUsed);
        return quotaUsed <= rateLimitStatus.getQuotaRemaining();
    }

    private boolean isExistingPerson(List<CloudPerson> personList, CloudPerson targetPerson) {
        return personList.stream()
                .filter(person -> person.getFirstName().equals(targetPerson.getFirstName())
                        && person.getLastName().equals(targetPerson.getLastName()))
                .findAny()
                .isPresent();
    }

    private boolean isExistingTag(List<CloudTag> tagList, CloudTag targetTag) {
        return tagList.stream()
                .filter(tag -> !targetTag.getName().equals(targetTag.getName()))
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
     * @param personList
     * @param newPerson
     * @return newPerson, if added, else null
     */
    private CloudPerson addPerson(List<CloudPerson> personList, CloudPerson newPerson) throws JAXBException {
        if (newPerson == null) throw new IllegalArgumentException("Person cannot be null");
        String newPersonFirstName = newPerson.getFirstName();
        String newPersonLastName = newPerson.getLastName();
        if (newPersonFirstName == null || newPersonLastName == null) {
            throw new IllegalArgumentException("Fields cannot be null");
        }
        if (isExistingPerson(personList, newPerson)) throw new IllegalArgumentException("Person already exists");

        personList.add(newPerson);

        return newPerson;
    }

    private Optional<CloudPerson> getPerson(List<CloudPerson> personList, String firstName, String lastName) {
        return personList.stream()
                .filter(person -> person.getFirstName().equals(firstName)
                        && person.getLastName().equals(lastName))
                .findAny();
    }

    private CloudPerson updatePersonDetails(List<CloudPerson> personList, String oldFirstName, String oldLastName,
                                            CloudPerson updatedPerson) throws NoSuchElementException {
        CloudPerson oldPerson = getPersonIfExists(personList, oldFirstName, oldLastName);
        oldPerson.updatedBy(updatedPerson);
        return oldPerson;
    }

    private CloudPerson getPersonIfExists(List<CloudPerson> personList, String oldFirstName, String oldLastName) {
        Optional<CloudPerson> personQueryResult = getPerson(personList, oldFirstName, oldLastName);
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
        try {
            return new ByteArrayInputStream(JsonUtil.toJsonString(object).getBytes());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int getNumberOfRequestsRequired(int dataSize, int resourcesPerPage) {
        return (int) Math.ceil((double) dataSize / resourcesPerPage);
    }

    private void deletePersonFromData(List<CloudPerson> personList, String firstName, String lastName)
            throws NoSuchElementException {
        CloudPerson deletedPerson = getPersonIfExists(personList, firstName, lastName);
        deletedPerson.setDeleted(true);
    }

    private CloudTag addTag(List<CloudTag> tagList, CloudTag newTag) {
        if (newTag == null) throw new IllegalArgumentException("Tag cannot be null");
        String tagName = newTag.getName();
        if (tagName == null) throw new IllegalArgumentException("Fields cannot be null");
        if (isExistingTag(tagList, newTag)) throw new IllegalArgumentException("Tag already exists");
        tagList.add(newTag);
        return newTag;
    }

    private Optional<CloudTag> getTag(List<CloudTag> tagList, String tagName) {
        return tagList.stream()
                .filter(tag -> tag.getName().equals(tagName))
                .findAny();
    }

    private CloudTag getTagIfExists(List<CloudTag> tagList, String tagName) {
        Optional<CloudTag> tagQueryResult = getTag(tagList, tagName);
        if (!tagQueryResult.isPresent()) throw new NoSuchElementException("No such tag found.");

        return tagQueryResult.get();
    }

    private CloudTag updateTagDetails(List<CloudTag> tagList, String oldTagName, CloudTag updatedTag)
            throws NoSuchElementException {
        CloudTag oldTag = getTagIfExists(tagList, oldTagName);
        oldTag.updatedBy(updatedTag);
        return oldTag;
    }

    private void deleteTagFromData(List<CloudTag> tagList, String tagName) throws NoSuchElementException {
        CloudTag tag = getTagIfExists(tagList, tagName);
        // This may differ from how GitHub does it, but we won't know for sure
        tagList.remove(tag);
    }
}
