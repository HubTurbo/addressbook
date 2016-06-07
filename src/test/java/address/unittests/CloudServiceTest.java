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
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CloudServiceTest {
    private static int RESOURCES_PER_PAGE = 100;

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
        int quotaLimit = 10;
        int quotaRemaining = 9;
        List<CloudPerson> personsToReturn = new ArrayList<>();
        personsToReturn.add(new CloudPerson("firstName", "lastName"));

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, LocalDateTime.now().toEpochSecond(getSystemTimezone()) + 30000);
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_OK, personsToReturn, header);
        when(cloudSimulator.getPersons("Test", 1, RESOURCES_PER_PAGE, null)).thenReturn(cloudResponse);

        ExtractedCloudResponse<List<Person>> serviceResponse = cloudService.getPersons("Test");
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(1, serviceResponse.getData().get().size());
        assertEquals("firstName", serviceResponse.getData().get().get(0).getFirstName());
        assertEquals("lastName", serviceResponse.getData().get().get(0).getLastName());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void testGetPersons_multiplePages_successfulQuery() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 0;
        int noOfPersons = 1000;
        List<CloudPerson> personsToReturn = new ArrayList<>();
        for (int i = 0; i < noOfPersons; i++) {
            personsToReturn.add(new CloudPerson("firstName" + i, "lastName" + i));
        }

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, LocalDateTime.now().toEpochSecond(getSystemTimezone()) + 30000);
        when(cloudSimulator.getPersons(anyString(), anyInt(), anyInt(), anyObject())).thenAnswer((invocation) -> {
            Object[] args = invocation.getArguments();
            String addressBookName = (String) args[0];
            assertEquals("Test", addressBookName);
            int pageNumber = (int) args[1];
            int resourcesPerPage = (int) args[2];
            int startIndex = (pageNumber - 1) * resourcesPerPage;
            int endIndex = pageNumber * resourcesPerPage;

            RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_OK, personsToReturn.subList(startIndex, endIndex), header);

            pageNumber = (pageNumber < 1 ? 1 : pageNumber);
            int totalPages = (int) Math.ceil(noOfPersons/RESOURCES_PER_PAGE);
            if (totalPages < 1 || pageNumber > totalPages) return cloudResponse;
            if (pageNumber < totalPages) cloudResponse.setNextPageNo(pageNumber + 1);
            if (pageNumber > 1) cloudResponse.setPreviousPageNo(pageNumber - 1);
            cloudResponse.setFirstPageNo(1);
            cloudResponse.setLastPageNo(totalPages);

            return cloudResponse;
        });


        ExtractedCloudResponse<List<Person>> serviceResponse = cloudService.getPersons("Test");
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(noOfPersons, serviceResponse.getData().get().size());

        for (int i = 0; i < noOfPersons; i++) {
            assertEquals("firstName" + i, serviceResponse.getData().get().get(i).getFirstName());
            assertEquals("lastName" + i, serviceResponse.getData().get().get(i).getLastName());
        }
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }
}
