package address.unittests;

import address.model.datatypes.Person;
import address.sync.CloudService;
import address.sync.CloudSimulator;
import address.sync.ExtractedCloudResponse;
import address.sync.RawCloudResponse;
import address.sync.model.CloudPerson;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CloudServiceTest {
    CloudService cloudService;
    CloudSimulator cloudSimulator;

    private HashMap<String, String> getHeader(int limit, int remaining, long reset) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("X-RateLimit-Limit", String.valueOf(limit));
        headers.put("X-RateLimit-Remaining", String.valueOf(remaining));
        headers.put("X-RateLimit-Reset", String.valueOf(reset));
        return headers;
    }

    private ZoneOffset getSystemTimezone() {
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneOffset.systemDefault());
        return zonedDateTime.getOffset();
    }

    @Before
    public void setup() {
        cloudSimulator = mock(CloudSimulator.class);
        cloudService = new CloudService(cloudSimulator);
    }

    @Test
    public void testGetPersons() throws IOException {
        List<CloudPerson> personsToReturn = new ArrayList<>();
        personsToReturn.add(new CloudPerson("firstName", "lastName"));

        HashMap<String, String> header = getHeader(10, 10, LocalDateTime.now().toEpochSecond(getSystemTimezone()));
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_OK, personsToReturn, header);
        when(cloudSimulator.getPersons("Test", 1, 100, null)).thenReturn(cloudResponse);

        ExtractedCloudResponse<List<Person>> serviceResponse = cloudService.getPersons("Test");
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(1, serviceResponse.getData().get().size());
        assertEquals("firstName", serviceResponse.getData().get().get(0).getFirstName());
        assertEquals("lastName", serviceResponse.getData().get().get(0).getLastName());
    }
}
