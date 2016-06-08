package address.unittests;

import address.sync.CloudFileHandler;
import address.sync.CloudRateLimitStatus;
import address.sync.CloudSimulator;
import address.sync.RawCloudResponse;
import address.sync.model.CloudAddressBook;
import address.sync.model.CloudPerson;
import address.sync.model.CloudTag;
import address.util.JsonUtil;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class CloudSimulatorTest {

    private static int STARTING_API_COUNT = 10;
    private static int API_RESET_DELAY = 30000;

    CloudFileHandler cloudFileHandler;
    CloudRateLimitStatus cloudRateLimitStatus;
    CloudSimulator cloudSimulator;

    /**
     * Mocks the file handler and spies on the limit status
     *
     * readCloudAddressBookFromFile is also stubbed to return a pre-defined
     * dummy addressbook
     *
     * @throws JAXBException
     */
    @Before
    public void setup() throws JAXBException {
        final long resetTime = System.currentTimeMillis()/1000 + API_RESET_DELAY;
        cloudFileHandler = mock(CloudFileHandler.class);
        cloudRateLimitStatus = new CloudRateLimitStatus(STARTING_API_COUNT, resetTime);
        cloudSimulator = new CloudSimulator(cloudFileHandler, cloudRateLimitStatus, false);

        CloudAddressBook cloudAddressBook = getDummyAddressBook();
        stub(cloudFileHandler.readCloudAddressBookFromFile("Test")).toReturn(cloudAddressBook);

        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());
    }

    @Test
    public void createAddressBook() throws IOException, JAXBException {
        final int apiUsage = 1;

        RawCloudResponse cloudResponse = cloudSimulator.createAddressBook("Test");
        verify(cloudFileHandler, times(1)).createCloudAddressBookFile("Test");
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_CREATED, cloudResponse.getResponseCode());
    }

    @Test
    public void createAddressBook_notEnoughQuota_unsuccessfulCreation() throws IOException, JAXBException {
        // Use up quota
        cloudRateLimitStatus.useQuota(STARTING_API_COUNT);

        assertEquals(0, cloudRateLimitStatus.getQuotaRemaining());
        RawCloudResponse cloudResponse = cloudSimulator.createAddressBook("Test");
        verify(cloudFileHandler, never()).createCloudAddressBookFile("Test");
        assertEquals(0, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, cloudResponse.getResponseCode());
    }

    @Test
    public void createAddressBook_illegalArgument_unsuccessfulCreation() throws IOException, JAXBException {
        int apiUsage = 1;

        doThrow(new IllegalArgumentException("AddressBook 'Test' already exists!")).when(cloudFileHandler).createCloudAddressBookFile("Test");

        RawCloudResponse cloudResponse = cloudSimulator.createAddressBook("Test");
        verify(cloudFileHandler, times(1)).createCloudAddressBookFile("Test");
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, cloudResponse.getResponseCode());
    }

    @Test
    public void createAddressBook_marshalException_unsuccessfulCreation() throws IOException, JAXBException {
        doThrow(new JAXBException("Error marshalling to file.")).when(cloudFileHandler).createCloudAddressBookFile("Test");

        RawCloudResponse cloudResponse = cloudSimulator.createAddressBook("Test");
        verify(cloudFileHandler, times(1)).createCloudAddressBookFile("Test");

        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, cloudResponse.getResponseCode());
    }

    @Test
    public void deletePerson_enoughQuota_successfulDeletion() throws IOException, JAXBException {
        final int apiUsage = 1;

        RawCloudResponse cloudResponse = cloudSimulator.deletePerson("Test", "firstName", "lastName");
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(any(CloudAddressBook.class));
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, cloudResponse.getResponseCode());
    }

    @Test
    public void deletePerson_noSuchPerson_unsuccessfulDeletion() throws JAXBException {
        int apiUsage = 1;

        RawCloudResponse cloudResponse = cloudSimulator.deletePerson("Test", "unknownName", "unknownName");
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, cloudResponse.getResponseCode());
    }

    @Test
    public void deletePerson_marshalException_unsuccessfulDeletion() throws IOException, JAXBException {
        doThrow(new JAXBException("Error marshalling to file.")).when(cloudFileHandler).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        RawCloudResponse cloudResponse = cloudSimulator.deletePerson("Test", "firstName", "lastName");
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(any(CloudAddressBook.class));
        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, cloudResponse.getResponseCode());
    }

    @Test
    public void updatePerson() throws JAXBException {
        final int apiUsage = 1;

        CloudPerson updatedPerson = prepareUpdatedPerson();

        RawCloudResponse cloudResponse = cloudSimulator.updatePerson("Test", "firstName", "lastName", updatedPerson, null);
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(any(CloudAddressBook.class));
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());
    }

    @Test
    public void updatePerson_notEnoughQuota_unsuccessfulUpdate() throws JAXBException {
        // Use up quota
        cloudRateLimitStatus.useQuota(STARTING_API_COUNT);

        CloudPerson updatedPerson = prepareUpdatedPerson();

        assertEquals(0, cloudRateLimitStatus.getQuotaRemaining());
        RawCloudResponse cloudResponse = cloudSimulator.updatePerson("Test", "firstName", "lastName", updatedPerson, null);
        verify(cloudFileHandler, never()).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));
        assertEquals(0, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, cloudResponse.getResponseCode());
    }

    @Test
    public void updatePerson_noSuchTag_successfulUpdateAndAddedTag() throws JAXBException {
        final int apiUsage = 1;

        // Tag a person with a new tag not yet defined
        CloudPerson updatedPerson = prepareUpdatedPerson();
        CloudTag newTag = new CloudTag("New Tag");
        List<CloudTag> tagList = new ArrayList<>();
        tagList.add(newTag);
        updatedPerson.setTags(tagList);

        // person should be updated and tag should be added to the list of tags
        List<CloudPerson> updatedPersonList = new ArrayList<>();
        updatedPersonList.add(updatedPerson);
        List<CloudTag> updatedTagList = new ArrayList<>();
        updatedTagList.add(new CloudTag("Tag one"));
        updatedTagList.add(newTag);
        CloudAddressBook updatedAddressBook = new CloudAddressBook("Test", updatedPersonList, updatedTagList);

        RawCloudResponse cloudResponse = cloudSimulator.updatePerson("Test", "firstName", "lastName", updatedPerson, null);
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(updatedAddressBook);
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());
    }

    @Test
    public void addTag() throws JAXBException, IOException {
        final int apiUsage = 1;

        CloudTag newTag = new CloudTag("New Tag");

        CloudAddressBook updatedCloudAddressBook = getDummyAddressBook();
        updatedCloudAddressBook.getAllTags().add(newTag);

        RawCloudResponse cloudResponse = cloudSimulator.createTag("Test", newTag, null);
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(updatedCloudAddressBook);
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_CREATED, cloudResponse.getResponseCode());
        CloudTag cloudTag = JsonUtil.fromJsonString(convertToString(cloudResponse.getBody()), CloudTag.class);
        assertEquals(newTag, cloudTag);
    }

    @Test
    public void editTag() throws JAXBException {
        final int apiUsage = 1;

        CloudTag updatedTag = new CloudTag("Updated tag");

        CloudAddressBook updatedCloudAddressBook = getDummyAddressBook();
        updatedCloudAddressBook.getAllTags().remove(new CloudTag("Tag one"));
        updatedCloudAddressBook.getAllTags().add(updatedTag);

        RawCloudResponse cloudResponse = cloudSimulator.editTag("Test", "Tag one", updatedTag, null);
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(updatedCloudAddressBook);
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());
    }

    @Test
    public void deleteTag() throws JAXBException {
        final int apiUsage = 1;

        CloudAddressBook resultingAddressBook = getDummyAddressBook();
        resultingAddressBook.getAllTags().remove(new CloudTag("Tag one"));

        RawCloudResponse cloudResponse = cloudSimulator.deleteTag("Test", "Tag one");
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(resultingAddressBook);
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, cloudResponse.getResponseCode());
    }

    @Test
    public void getTags() throws JAXBException, IOException {
        final int apiUsage = 1;
        final int pageNumber = 11;
        final int resourcesPerPage = 20;

        CloudAddressBook bigCloudAddressBook = getBigDummyAddressBook();
        stub(cloudFileHandler.readCloudAddressBookFromFile("Big Test")).toReturn(bigCloudAddressBook);

        RawCloudResponse cloudResponse = cloudSimulator.getTags("Big Test", pageNumber, resourcesPerPage, null);

        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Big Test");
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(bigCloudAddressBook);
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());

        List<CloudTag> tagList = JsonUtil.fromJsonStringToList(convertToString(cloudResponse.getBody()), CloudTag.class);
        assertEquals(resourcesPerPage, tagList.size());
        for (int i = (pageNumber - 1) * resourcesPerPage; i < pageNumber * resourcesPerPage; i++) {
            assertTrue(tagList.contains(new CloudTag("Tag" + i)));
        }
        assertEquals(pageNumber + 1, cloudResponse.getNextPageNo());
        assertEquals(pageNumber - 1, cloudResponse.getPreviousPageNo());

        assertEquals(1, cloudResponse.getFirstPageNo());
        assertEquals((int) Math.ceil(1000/resourcesPerPage), cloudResponse.getLastPageNo());
    }

    @Test
    public void getPersons() throws JAXBException, IOException {
        final int apiUsage = 1;
        final int pageNumber = 12;
        final int resourcesPerPage = 30;

        CloudAddressBook bigCloudAddressBook = getBigDummyAddressBook();
        stub(cloudFileHandler.readCloudAddressBookFromFile("Big Test")).toReturn(bigCloudAddressBook);

        RawCloudResponse cloudResponse = cloudSimulator.getPersons("Big Test", pageNumber, resourcesPerPage, null);
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Big Test");
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(bigCloudAddressBook);
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());

        List<CloudPerson> personList = JsonUtil.fromJsonStringToList(convertToString(cloudResponse.getBody()), CloudPerson.class);
        assertEquals(resourcesPerPage, personList.size());
        for (int i = (pageNumber - 1) * resourcesPerPage; i < pageNumber * resourcesPerPage; i++) {
            assertTrue(personList.contains(new CloudPerson("firstName" + i, "lastName" + i)));
        }
        assertEquals(pageNumber + 1, cloudResponse.getNextPageNo());
        assertEquals(pageNumber - 1, cloudResponse.getPreviousPageNo());

        assertEquals(1, cloudResponse.getFirstPageNo());
        assertEquals((int) Math.ceil(2000/resourcesPerPage), cloudResponse.getLastPageNo());
    }

    @Test
    public void getPersons_sameRequest_notModifiedResponse() throws JAXBException {
        final int apiUsage = 1;
        final int pageNumber = 12;
        final int resourcesPerPage = 30;

        CloudAddressBook bigCloudAddressBook = getBigDummyAddressBook();
        stub(cloudFileHandler.readCloudAddressBookFromFile("Big Test")).toReturn(bigCloudAddressBook).toReturn(bigCloudAddressBook);

        RawCloudResponse cloudResponse = cloudSimulator.getPersons("Big Test", pageNumber, resourcesPerPage, null);
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Big Test");
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(bigCloudAddressBook);
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());

        String responseETag = cloudResponse.getHeaders().get("ETag");
        RawCloudResponse cloudResponse2 = cloudSimulator.getPersons("Big Test", pageNumber, resourcesPerPage, responseETag);
        verify(cloudFileHandler, times(2)).readCloudAddressBookFromFile("Big Test");
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(bigCloudAddressBook);
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_NOT_MODIFIED, cloudResponse2.getResponseCode());
    }

    @Test
    public void getRateLimitStatus() throws JAXBException {
        RawCloudResponse cloudResponse = cloudSimulator.getRateLimitStatus(null);
        verify(cloudFileHandler, never()).readCloudAddressBookFromFile("Big Test");
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));
        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());
    }

    @Test
    public void getUpdatedPersons() throws JAXBException, IOException {
        final int apiUsage = 1;
        final int pageNumber = 1;
        final int resourcesPerPage = 30;

        CloudAddressBook bigCloudAddressBook = getBigDummyAddressBook();
        stub(cloudFileHandler.readCloudAddressBookFromFile("Big Test")).toReturn(bigCloudAddressBook);

        String cutOffTime = LocalDateTime.now().toString();
        // update a person
        CloudPerson updatedPerson = new CloudPerson("firstName353", "lastName353");
        bigCloudAddressBook.getAllPersons().get(352).updatedBy(updatedPerson);

        // get updated persons since response time
        RawCloudResponse cloudResponse = cloudSimulator.getUpdatedPersons("Big Test", cutOffTime, pageNumber, resourcesPerPage, null);

        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Big Test");
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());

        List<CloudPerson> personList = JsonUtil.fromJsonStringToList(convertToString(cloudResponse.getBody()), CloudPerson.class);
        // should only return the updated person
        assertEquals(1, personList.size());
        assertTrue(personList.contains(new CloudPerson("firstName353", "lastName353")));
        assertEquals(-1, cloudResponse.getNextPageNo());
        assertEquals(-1, cloudResponse.getPreviousPageNo());
        assertEquals(1, cloudResponse.getFirstPageNo());
        assertEquals((int) Math.ceil(1/resourcesPerPage), cloudResponse.getLastPageNo());
    }

    @Test
    public void addPerson() throws JAXBException, IOException {
        final int apiUsage = 1;

        CloudAddressBook cloudAddressBook = getDummyAddressBook();
        stub(cloudFileHandler.readCloudAddressBookFromFile("Test")).toReturn(cloudAddressBook);

        CloudPerson cloudPerson = new CloudPerson("unknownName", "unknownName");
        RawCloudResponse cloudResponse = cloudSimulator.createPerson("Test", cloudPerson, null);
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_CREATED, cloudResponse.getResponseCode());

        CloudPerson person = JsonUtil.fromJsonString(convertToString(cloudResponse.getBody()), CloudPerson.class);
        assertEquals(cloudPerson, person);
    }

    private String convertToString(InputStream stream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder stringBuffer = new StringBuilder();
        while (reader.ready()) {
            stringBuffer.append(reader.readLine());
        }

        return stringBuffer.toString();
    }

    private CloudAddressBook getDummyAddressBook() {
        CloudAddressBook cloudAddressBook = new CloudAddressBook("Test");
        cloudAddressBook.getAllPersons().add(new CloudPerson("firstName", "lastName"));
        cloudAddressBook.getAllTags().add(new CloudTag("Tag one"));
        return cloudAddressBook;
    }

    private CloudAddressBook getBigDummyAddressBook() {
        int personsToGenerate = 2000;
        int tagsToGenerate = 1000;
        CloudAddressBook cloudAddressBook = new CloudAddressBook("Big Test");
        for (int i = 0; i < personsToGenerate; i++) {
            cloudAddressBook.getAllPersons().add(new CloudPerson("firstName" + i, "lastName" + i));
        }

        for (int i = 0; i < tagsToGenerate; i++) {
            cloudAddressBook.getAllTags().add(new CloudTag("Tag" + i));
        }
        return cloudAddressBook;
    }

    private CloudPerson prepareUpdatedPerson() {
        CloudPerson updatedPerson = new CloudPerson("firstName", "lastName");
        updatedPerson.setCity("Singapore");
        return updatedPerson;
    }
}
