package address.unittests.sync;

import address.model.datatypes.person.Person;
import address.model.datatypes.tag.Tag;
import address.sync.cloud.RemoteResponse;
import address.sync.RemoteService;
import address.sync.cloud.CloudSimulator;
import address.sync.ExtractedRemoteResponse;
import address.sync.cloud.model.CloudPerson;
import address.sync.cloud.model.CloudTag;
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

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RemoteServiceTest {
    private static int RESOURCES_PER_PAGE = 100;

    RemoteService remoteService;
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
        remoteService = new RemoteService(cloudSimulator);
    }

    @Test
    public void getPersons() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;
        List<CloudPerson> personsToReturn = new ArrayList<>();
        CloudPerson personToReturn = new CloudPerson("firstName", "lastName");
        personToReturn.setId(1);
        personsToReturn.add(personToReturn);

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_OK, personsToReturn, header);
        when(cloudSimulator.getPersons("Test", 1, RESOURCES_PER_PAGE, null)).thenReturn(remoteResponse);

        ExtractedRemoteResponse<List<Person>> serviceResponse = remoteService.getPersons("Test", 1);
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(1, serviceResponse.getData().get().size());
        assertEquals("firstName", serviceResponse.getData().get().get(0).getFirstName());
        assertEquals("lastName", serviceResponse.getData().get().get(0).getLastName());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void getPersons_errorCloudResponse() throws IOException {
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(cloudSimulator.getPersons("Test", 1, RESOURCES_PER_PAGE, null)).thenReturn(remoteResponse);

        ExtractedRemoteResponse<List<Person>> serviceResponse = remoteService.getPersons("Test", 1);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(0, serviceResponse.getQuotaLimit());
        assertEquals(0, serviceResponse.getQuotaRemaining());
        assertNull(serviceResponse.getQuotaResetTime());
    }

    @Test
    public void getPersons_multiplePages_successfulQuery() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 0;
        int noOfPersons = 1000;
        List<CloudPerson> personsToReturn = new ArrayList<>();
        for (int i = 0; i < noOfPersons; i++) {
            personsToReturn.add(new CloudPerson("firstName" + i, "lastName" + i));
        }

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        when(cloudSimulator.getPersons(anyString(), anyInt(), anyInt(), anyObject())).thenAnswer((invocation) -> {
            Object[] args = invocation.getArguments();
            String addressBookName = (String) args[0];
            assertEquals("Test", addressBookName);
            int pageNumber = (int) args[1];
            int resourcesPerPage = (int) args[2];
            int startIndex = (pageNumber - 1) * resourcesPerPage;
            int endIndex = pageNumber * resourcesPerPage;

            RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_OK, personsToReturn.subList(startIndex, endIndex), header);

            pageNumber = pageNumber < 1 ? 1 : pageNumber;
            int lastPage = (int) Math.ceil(noOfPersons/RESOURCES_PER_PAGE);
            remoteResponse.setFirstPageNo(1);
            remoteResponse.setLastPageNo(lastPage);
            if (pageNumber < lastPage) remoteResponse.setNextPageNo(pageNumber + 1);
            if (pageNumber > 1) remoteResponse.setPreviousPageNo(pageNumber - 1);

            return remoteResponse;
        });


        ExtractedRemoteResponse<List<Person>> serviceResponse = remoteService.getPersons("Test", 4);
        assertEquals(HttpURLConnection.HTTP_OK, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(RESOURCES_PER_PAGE, serviceResponse.getData().get().size());

        for (int i = 0; i < RESOURCES_PER_PAGE; i++) {
            assertEquals("firstName" + (i + 400), serviceResponse.getData().get().get(i).getFirstName());
            assertEquals("lastName" + (i + 400), serviceResponse.getData().get().get(i).getLastName());
        }
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    private long getResetTime() {
        return LocalDateTime.now().toEpochSecond(getSystemTimezone()) + 30000;
    }

    @Test
    public void createPerson() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        CloudPerson remotePerson = new CloudPerson("unknownName", "unknownName");
        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_CREATED, remotePerson, header);
        when(cloudSimulator.createPerson(anyString(), any(CloudPerson.class), isNull(String.class))).thenReturn(remoteResponse);

        Person person = new Person("unknownName", "unknownName");

        ExtractedRemoteResponse<Person> serviceResponse = remoteService.createPerson("Test", person);
        assertEquals(HttpURLConnection.HTTP_CREATED, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(person, serviceResponse.getData().get());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void createPerson_errorCloudResponse() throws IOException {
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(cloudSimulator.createPerson(anyString(), any(CloudPerson.class), isNull(String.class))).thenReturn(remoteResponse);

        Person person = new Person("unknownName", "unknownName");

        ExtractedRemoteResponse<Person> serviceResponse = remoteService.createPerson("Test", person);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(0, serviceResponse.getQuotaLimit());
        assertEquals(0, serviceResponse.getQuotaRemaining());
        assertNull(serviceResponse.getQuotaResetTime());
    }

    @Test
    public void createPerson_newTag_successfulCreationOfPersonAndTag() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        CloudPerson remotePerson = new CloudPerson("unknownName", "unknownName");
        List<CloudTag> personCloudTags = new ArrayList<>();
        personCloudTags.add(new CloudTag("New Tag"));
        remotePerson.setTags(personCloudTags);
        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_CREATED, remotePerson, header);
        when(cloudSimulator.createPerson(anyString(), any(CloudPerson.class), isNull(String.class))).thenReturn(remoteResponse);

        Person person = new Person("unknownName", "unknownName");
        List<Tag> personTags = new ArrayList<>();
        personTags.add(new Tag("New Tag"));
        person.setTags(personTags);

        ExtractedRemoteResponse<Person> serviceResponse = remoteService.createPerson("Test", person);
        assertEquals(HttpURLConnection.HTTP_CREATED, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        // this only checks for name equality
        assertEquals(person, serviceResponse.getData().get());
        assertEquals(person.getTagList().size(), serviceResponse.getData().get().getObservableTagList().size());
        assertEquals(person.getTagList().get(0).getName(), serviceResponse.getData().get().getObservableTagList().get(0).getName());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void createTag() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        CloudTag remoteTag = new CloudTag("New Tag");
        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_CREATED, remoteTag, header);
        when(cloudSimulator.createTag(anyString(), any(CloudTag.class), isNull(String.class))).thenReturn(remoteResponse);

        Tag tag = new Tag("New Tag");

        ExtractedRemoteResponse<Tag> serviceResponse = remoteService.createTag("Test", tag);
        assertEquals(HttpURLConnection.HTTP_CREATED, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(tag, serviceResponse.getData().get());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void createTag_errorCloudResponse() throws IOException {
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(cloudSimulator.createTag(anyString(), any(CloudTag.class), isNull(String.class))).thenReturn(remoteResponse);

        Tag tag = new Tag("New Tag");

        ExtractedRemoteResponse<Tag> serviceResponse = remoteService.createTag("Test", tag);
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(0, serviceResponse.getQuotaLimit());
        assertEquals(0, serviceResponse.getQuotaRemaining());
        assertNull(serviceResponse.getQuotaResetTime());
    }

    @Test
    public void getTags() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        List<CloudTag> tagList = new ArrayList<>();
        tagList.add(new CloudTag("tagName"));

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_OK, tagList, header);
        when(cloudSimulator.getTags(anyString(), anyInt(), anyInt(), isNull(String.class))).thenReturn(remoteResponse);

        ExtractedRemoteResponse<List<Tag>> serviceResponse = remoteService.getTags("Test", 1, null);

        assertEquals(HttpURLConnection.HTTP_OK, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(1, serviceResponse.getData().get().size());
        assertEquals("tagName", serviceResponse.getData().get().get(0).getName());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void updatePerson() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        CloudPerson remotePerson = new CloudPerson("newFirstName", "newLastName");
        remotePerson.setId(1);

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_OK, remotePerson, header);
        when(cloudSimulator.updatePerson(anyString(), anyInt(), any(CloudPerson.class), isNull(String.class))).thenReturn(remoteResponse);

        Person updatedPerson = new Person("newFirstName", "newLastName");
        ExtractedRemoteResponse<Person> serviceResponse = remoteService.updatePerson("Test", 1, updatedPerson);

        assertEquals(HttpURLConnection.HTTP_OK, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals("newFirstName", serviceResponse.getData().get().getFirstName());
        assertEquals("newLastName", serviceResponse.getData().get().getLastName());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void updatePerson_errorCloudResponse() throws IOException {
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(cloudSimulator.updatePerson(anyString(), anyInt(), any(CloudPerson.class), isNull(String.class))).thenReturn(remoteResponse);

        Person updatedPerson = new Person("newFirstName", "newLastName");
        ExtractedRemoteResponse<Person> serviceResponse = remoteService.updatePerson("Test", 1, updatedPerson);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(0, serviceResponse.getQuotaLimit());
        assertEquals(0, serviceResponse.getQuotaRemaining());
        assertNull(serviceResponse.getQuotaResetTime());
    }

    @Test
    public void editTag() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        CloudTag remoteTag = new CloudTag("newTagName");
        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_OK, remoteTag, header);
        when(cloudSimulator.editTag(anyString(), anyString(), any(CloudTag.class), isNull(String.class))).thenReturn(remoteResponse);

        Tag updatedTag = new Tag("newTagName");
        ExtractedRemoteResponse<Tag> serviceResponse = remoteService.editTag("Test", "tagName", updatedTag);

        assertEquals(HttpURLConnection.HTTP_OK, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals("newTagName", serviceResponse.getData().get().getName());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void editTag_errorCloudResponse() throws IOException {
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(cloudSimulator.editTag(anyString(), anyString(), any(CloudTag.class), isNull(String.class))).thenReturn(remoteResponse);

        Tag updatedTag = new Tag("newTagName");
        ExtractedRemoteResponse<Tag> serviceResponse = remoteService.editTag("Test", "tagName", updatedTag);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(0, serviceResponse.getQuotaLimit());
        assertEquals(0, serviceResponse.getQuotaRemaining());
        assertNull(serviceResponse.getQuotaResetTime());
    }

    @Test
    public void deletePerson() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_NO_CONTENT, null, header);
        when(cloudSimulator.deletePerson("Test", 1)).thenReturn(remoteResponse);

        ExtractedRemoteResponse<Void> serviceResponse = remoteService.deletePerson("Test", 1);

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void deletePerson_errorCloudResponse() throws IOException {
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(cloudSimulator.deletePerson("Test", 1)).thenReturn(remoteResponse);

        ExtractedRemoteResponse<Void> serviceResponse = remoteService.deletePerson("Test", 1);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(0, serviceResponse.getQuotaLimit());
        assertEquals(0, serviceResponse.getQuotaRemaining());
        assertNull(serviceResponse.getQuotaResetTime());
    }

    @Test
    public void deleteTag() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_NO_CONTENT, null, header);
        when(cloudSimulator.deleteTag("Test", "tagName")).thenReturn(remoteResponse);

        ExtractedRemoteResponse<Void> serviceResponse = remoteService.deleteTag("Test", "tagName");

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void deleteTag_errorCloudResponse() throws IOException {
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(cloudSimulator.deleteTag("Test", "tagName")).thenReturn(remoteResponse);

        ExtractedRemoteResponse<Void> serviceResponse = remoteService.deleteTag("Test", "tagName");

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(0, serviceResponse.getQuotaLimit());
        assertEquals(0, serviceResponse.getQuotaRemaining());
        assertNull(serviceResponse.getQuotaResetTime());
    }

    @Test
    public void createAddressBook() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_CREATED, null, header);
        when(cloudSimulator.createAddressBook("Test")).thenReturn(remoteResponse);

        ExtractedRemoteResponse<Void> serviceResponse = remoteService.createAddressBook("Test");

        assertEquals(HttpURLConnection.HTTP_CREATED, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void getUpdatedPersonsSince() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        LocalDateTime cutOffTime = LocalDateTime.now();
        List<CloudPerson> remotePersons = new ArrayList<>();
        remotePersons.add(new CloudPerson("firstName", "lastName"));

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_OK, remotePersons, header);
        when(cloudSimulator.getUpdatedPersons(anyString(), anyString(), anyInt(), anyInt(), anyString())).thenReturn(remoteResponse);

        ExtractedRemoteResponse<List<Person>> serviceResponse = remoteService.getUpdatedPersonsSince("Test", 1, cutOffTime, null);

        assertEquals(HttpURLConnection.HTTP_OK, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(1, serviceResponse.getData().get().size());
        assertEquals("firstName", serviceResponse.getData().get().get(0).getFirstName());
        assertEquals("lastName", serviceResponse.getData().get().get(0).getLastName());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void getUpdatedPersonsSince_manyData_successfulGet() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 8;

        LocalDateTime cutOffTime = LocalDateTime.now();

        // response 1
        List<CloudPerson> remotePersons = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            remotePersons.add(new CloudPerson("firstName" + i, "lastName" + i));
        }
        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining + 1, getResetTime());
        RemoteResponse remoteResponseOne = new RemoteResponse(HttpURLConnection.HTTP_OK, remotePersons, header);
        remoteResponseOne.setFirstPageNo(1);
        remoteResponseOne.setNextPageNo(2);
        remoteResponseOne.setLastPageNo(2);

        // response 2
        List<CloudPerson> remotePersons2 = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            remotePersons2.add(new CloudPerson("firstName" + (i + 100), "lastName" + (i + 100)));
        }
        HashMap<String, String> header2 = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RemoteResponse remoteResponseTwo = new RemoteResponse(HttpURLConnection.HTTP_OK, remotePersons2, header2);
        remoteResponseTwo.setFirstPageNo(1);
        remoteResponseTwo.setPreviousPageNo(1);
        remoteResponseTwo.setLastPageNo(2);

        when(cloudSimulator.getUpdatedPersons(anyString(), anyString(), anyInt(), anyInt(), anyString())).thenReturn(remoteResponseTwo);

        ExtractedRemoteResponse<List<Person>> serviceResponse = remoteService.getUpdatedPersonsSince("Test", 2, cutOffTime, null);

        assertEquals(HttpURLConnection.HTTP_OK, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(50, serviceResponse.getData().get().size());
        for (int i = 0; i < 50; i++) {
            assertEquals("firstName" + (i + 100), serviceResponse.getData().get().get(i).getFirstName());
            assertEquals("lastName" + (i + 100), serviceResponse.getData().get().get(i).getLastName());
        }
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void getUpdatedPersonsSince_errorCloudResponse_returnEmptyResponse() throws IOException {
        LocalDateTime cutOffTime = LocalDateTime.now();

        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(cloudSimulator.getUpdatedPersons(anyString(), anyString(), anyInt(), anyInt(), anyString())).thenReturn(remoteResponse);

        ExtractedRemoteResponse<List<Person>> serviceResponse = remoteService.getUpdatedPersonsSince("Test", 1, cutOffTime, null);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(0, serviceResponse.getQuotaLimit());
        assertEquals(0, serviceResponse.getQuotaRemaining());
        assertNull(serviceResponse.getQuotaResetTime());
    }

    @Test
    public void getLimitStatus() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 10;
        long resetTime = getResetTime();

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, resetTime);
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_OK, header, header);
        when(cloudSimulator.getRateLimitStatus(isNull(String.class))).thenReturn(remoteResponse);
        ExtractedRemoteResponse<HashMap<String, String>> serviceResponse = remoteService.getLimitStatus();

        assertEquals(HttpURLConnection.HTTP_OK, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(3, serviceResponse.getData().get().size());
        assertEquals("10", serviceResponse.getData().get().get("Limit"));
        assertEquals("10", serviceResponse.getData().get().get("Remaining"));
        assertEquals(String.valueOf(resetTime), serviceResponse.getData().get().get("Reset"));
    }

    @Test
    public void getLimitStatus_errorCloudResponse() throws IOException {
        RemoteResponse remoteResponse = new RemoteResponse(HttpURLConnection.HTTP_INTERNAL_ERROR);
        when(cloudSimulator.getRateLimitStatus(isNull(String.class))).thenReturn(remoteResponse);
        ExtractedRemoteResponse<HashMap<String, String>> serviceResponse = remoteService.getLimitStatus();

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(0, serviceResponse.getQuotaLimit());
        assertEquals(0, serviceResponse.getQuotaRemaining());
        assertNull(serviceResponse.getQuotaResetTime());
    }
}
