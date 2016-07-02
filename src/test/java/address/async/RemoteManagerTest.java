package address.async;

import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import address.sync.ExtractedRemoteResponse;
import address.sync.RemoteManager;
import address.sync.RemoteService;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RemoteManagerTest {
    private static final int RESOURCES_PER_PAGE = 100;
    private RemoteService remoteService;
    private RemoteManager remoteManager;

    @Before
    public void setup() {
        remoteService = mock(RemoteService.class);
        remoteManager = new RemoteManager(remoteService);
    }

    private ZoneOffset getSystemTimezone() {
        LocalDateTime localDateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneOffset.systemDefault());
        return zonedDateTime.getOffset();
    }

    private long getResetTime() {
        return LocalDateTime.now().toEpochSecond(getSystemTimezone()) + 30000;
    }

    @Test
    public void getUpdatedPersons_noPreviousQueryAndMultiplePages_successfulQuery() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 0;
        int noOfPersons = 1000;
        List<Person> personsToReturn = new ArrayList<>();
        for (int i = 0; i < noOfPersons; i++) {
            personsToReturn.add(new Person("firstName" + i, "lastName" + i, i));
        }

        when(remoteService.getPersons(anyString(), anyInt())).thenAnswer((invocation) -> {
            Object[] args = invocation.getArguments();
            String addressBookName = (String) args[0];
            assertEquals("Test", addressBookName);
            int pageNumber = (int) args[1];
            int resourcesPerPage = RESOURCES_PER_PAGE;
            int startIndex = (pageNumber - 1) * resourcesPerPage;
            int endIndex = pageNumber * resourcesPerPage;

            ExtractedRemoteResponse<List<Person>> remoteResponse = new ExtractedRemoteResponse<>(HttpURLConnection.HTTP_OK,
                    "eTag", quotaLimit, quotaRemaining, getResetTime(), personsToReturn.subList(startIndex, endIndex));

            pageNumber = pageNumber < 1 ? 1 : pageNumber;
            int lastPage = (int) Math.ceil(noOfPersons/RESOURCES_PER_PAGE);
            if (pageNumber < lastPage) {
                remoteResponse.setLastPage(lastPage);
                remoteResponse.setNextPage(pageNumber + 1);
            }
            if (pageNumber > 1) {
                remoteResponse.setFirstPage(1);
                remoteResponse.setPrevPage(pageNumber - 1);
            }

            return remoteResponse;
        });


        Optional<List<Person>> result = remoteManager.getUpdatedPersons("Test");

        // should return the full list of persons
        assertTrue(result.isPresent());
        assertEquals(noOfPersons, result.get().size());
        for (int i = 0; i < noOfPersons; i++) {
            assertEquals(i, result.get().get(i).getId());
            assertEquals("firstName" + i, result.get().get(i).getFirstName());
            assertEquals("lastName" + i, result.get().get(i).getLastName());
        }
    }


    @Test
    public void getTags_multiplePages_successfulGet() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 8;

        // response 1
        List<Tag> remoteTags = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            remoteTags.add(new Tag("tag" + i));
        }

        ExtractedRemoteResponse<List<Tag>> remoteResponseOne = new ExtractedRemoteResponse<>(HttpURLConnection.HTTP_OK,
                "eTag", quotaLimit, quotaRemaining, getResetTime(), remoteTags);
        remoteResponseOne.setFirstPage(1);
        remoteResponseOne.setNextPage(2);
        remoteResponseOne.setLastPage(2);

        // response 2
        List<Tag> remoteTags2 = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            remoteTags2.add(new Tag("tag" + (i + 100)));
        }
        ExtractedRemoteResponse<List<Tag>> remoteResponseTwo = new ExtractedRemoteResponse<>(HttpURLConnection.HTTP_OK,
                "eTag", quotaLimit, quotaRemaining, getResetTime(), remoteTags2);
        remoteResponseTwo.setFirstPage(1);
        remoteResponseTwo.setPrevPage(1);
        remoteResponseTwo.setLastPage(2);

        when(remoteService.getTags(anyString(), anyInt(), anyString())).thenReturn(remoteResponseOne).thenReturn(remoteResponseTwo);

        Optional<List<Tag>> result = remoteManager.getLatestTagList("Test");

        // should return the full list of tags
        assertTrue(result.isPresent());
        assertEquals(150, result.get().size());
        for (int i = 0; i < 150; i++) {
            assertEquals("tag" + i, result.get().get(i).getName());
        }
    }
}
