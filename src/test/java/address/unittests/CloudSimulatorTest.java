package address.unittests;

import address.sync.CloudFileHandler;
import address.sync.CloudRateLimitStatus;
import address.sync.CloudSimulator;
import address.sync.RawCloudResponse;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.HttpURLConnection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CloudSimulatorTest {

    @Test
    public void testAdd() throws IOException, JAXBException {
        final int apiUsage = 1;
        final int startingApi = 1;
        final long resetTime = System.currentTimeMillis()/1000 + 30000;

        CloudFileHandler cloudFileHandler = mock(CloudFileHandler.class);
        CloudRateLimitStatus cloudRateLimitStatus = new CloudRateLimitStatus(startingApi, resetTime);
        CloudRateLimitStatus spiedCloudRateLimitStatus = spy(cloudRateLimitStatus);
        boolean isUnreliableNetwork = false;
        CloudSimulator cloudSimulator = new CloudSimulator(cloudFileHandler, spiedCloudRateLimitStatus,
                                                           isUnreliableNetwork);


        assertEquals(startingApi, spiedCloudRateLimitStatus.getQuotaRemaining());
        RawCloudResponse cloudResponse = cloudSimulator.createAddressBook("Test");
        verify(cloudFileHandler, times(1)).createCloudAddressBookFile("Test");
        assertEquals(startingApi - apiUsage, spiedCloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_CREATED, cloudResponse.getResponseCode());
    }
}
