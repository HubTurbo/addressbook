package address.unittests;

import address.sync.CloudFileHandler;
import address.sync.CloudRateLimitStatus;
import address.sync.CloudSimulator;
import address.sync.RawCloudResponse;
import address.sync.model.CloudAddressBook;
import address.sync.model.CloudPerson;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CloudSimulatorTest {

    private static int STARTING_API_COUNT = 10;

    CloudFileHandler cloudFileHandler;
    CloudRateLimitStatus cloudRateLimitStatus;
    CloudSimulator cloudSimulator;

    @Before
    public void setup() {
        final long resetTime = System.currentTimeMillis()/1000 + 30000;
        cloudFileHandler = mock(CloudFileHandler.class);
        cloudRateLimitStatus = spy(new CloudRateLimitStatus(STARTING_API_COUNT, resetTime));
        cloudSimulator = new CloudSimulator(cloudFileHandler, cloudRateLimitStatus, false);
    }

    @Test
    public void createAddressBook_notEnoughQuota_unsuccessfulCreation() throws IOException, JAXBException {
        cloudRateLimitStatus.useQuota(STARTING_API_COUNT);

        assertEquals(0, cloudRateLimitStatus.getQuotaRemaining());
        RawCloudResponse cloudResponse = cloudSimulator.createAddressBook("Test");
        verify(cloudFileHandler, never()).createCloudAddressBookFile("Test");
        assertEquals(0, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, cloudResponse.getResponseCode());
    }

    @Test
    public void createAddressBook_enoughQuota_successfulCreation() throws IOException, JAXBException {
        final int apiUsage = 1;

        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());
        RawCloudResponse cloudResponse = cloudSimulator.createAddressBook("Test");
        verify(cloudFileHandler, times(1)).createCloudAddressBookFile("Test");
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_CREATED, cloudResponse.getResponseCode());
    }

    @Test
    public void deletePerson_enoughQuota_successfulDeletion() throws IOException, JAXBException {
        final int apiUsage = 1;

        CloudAddressBook cloudAddressBook = new CloudAddressBook("Test");
        cloudAddressBook.getAllPersons().add(new CloudPerson("firstName", "lastName"));

        stub(cloudFileHandler.readCloudAddressBookFromFile(("Test"))).toReturn(cloudAddressBook);

        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());
        RawCloudResponse cloudResponse = cloudSimulator.deletePerson("Test", "firstName", "lastName");
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(any(CloudAddressBook.class));
        assertEquals(STARTING_API_COUNT - apiUsage, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, cloudResponse.getResponseCode());
    }
}
