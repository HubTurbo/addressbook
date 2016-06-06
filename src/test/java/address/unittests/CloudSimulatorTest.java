package address.unittests;

import address.sync.CloudFileHandler;
import address.sync.CloudRateLimitStatus;
import address.sync.CloudSimulator;
import address.sync.RawCloudResponse;
import address.sync.model.CloudAddressBook;
import address.sync.model.CloudPerson;
import address.sync.model.CloudTag;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
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
        cloudRateLimitStatus = spy(new CloudRateLimitStatus(STARTING_API_COUNT, resetTime));
        cloudSimulator = new CloudSimulator(cloudFileHandler, cloudRateLimitStatus, false);

        CloudAddressBook cloudAddressBook = getDummyAddressBook();
        stub(cloudFileHandler.readCloudAddressBookFromFile(("Test"))).toReturn(cloudAddressBook);

        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());
    }

    private CloudAddressBook getDummyAddressBook() {
        CloudAddressBook cloudAddressBook = new CloudAddressBook("Test");
        cloudAddressBook.getAllPersons().add(new CloudPerson("firstName", "lastName"));
        return cloudAddressBook;
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
        RawCloudResponse cloudResponse = cloudSimulator.deletePerson("Test", "unknownName", "unknownName");
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));
        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, cloudResponse.getResponseCode());
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
        updatedTagList.add(newTag);
        CloudAddressBook updatedAddressBook = new CloudAddressBook("Test", updatedPersonList, updatedTagList);

        RawCloudResponse cloudResponse = cloudSimulator.updatePerson("Test", "firstName", "lastName", updatedPerson, null);
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(updatedAddressBook);
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());
    }

    private CloudPerson prepareUpdatedPerson() {
        CloudPerson updatedPerson = new CloudPerson("firstName", "lastName");
        updatedPerson.setCity("Singapore");
        return updatedPerson;
    }
}
