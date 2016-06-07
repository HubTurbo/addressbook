package address.unittests;

import address.model.datatypes.Person;
import address.model.datatypes.Tag;
import address.sync.CloudService;
import address.sync.CloudSimulator;
import address.sync.ExtractedCloudResponse;
import address.sync.RawCloudResponse;
import address.sync.model.CloudAddressBook;
import address.sync.model.CloudPerson;
import address.sync.model.CloudTag;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
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

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
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

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        when(cloudSimulator.getPersons(anyString(), anyInt(), anyInt(), anyObject())).thenAnswer((invocation) -> {
            Object[] args = invocation.getArguments();
            String addressBookName = (String) args[0];
            assertEquals("Test", addressBookName);
            int pageNumber = (int) args[1];
            int resourcesPerPage = (int) args[2];
            int startIndex = (pageNumber - 1) * resourcesPerPage;
            int endIndex = pageNumber * resourcesPerPage;

            RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_OK, personsToReturn.subList(startIndex, endIndex), header);

            pageNumber = pageNumber < 1 ? 1 : pageNumber;
            int lastPage = (int) Math.ceil(noOfPersons/RESOURCES_PER_PAGE);
            cloudResponse.setFirstPageNo(1);
            cloudResponse.setLastPageNo(lastPage);
            if (pageNumber < lastPage) cloudResponse.setNextPageNo(pageNumber + 1);
            if (pageNumber > 1) cloudResponse.setPreviousPageNo(pageNumber - 1);

            return cloudResponse;
        });


        ExtractedCloudResponse<List<Person>> serviceResponse = cloudService.getPersons("Test");
        assertEquals(HttpURLConnection.HTTP_OK, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(noOfPersons, serviceResponse.getData().get().size());

        for (int i = 0; i < noOfPersons; i++) {
            assertEquals("firstName" + i, serviceResponse.getData().get().get(i).getFirstName());
            assertEquals("lastName" + i, serviceResponse.getData().get().get(i).getLastName());
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

        CloudPerson cloudPerson = new CloudPerson("unknownName", "unknownName");
        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_CREATED, cloudPerson, header);
        when(cloudSimulator.createPerson(anyString(), any(CloudPerson.class), isNull(String.class))).thenReturn(cloudResponse);

        Person person = new Person("unknownName", "unknownName");

        ExtractedCloudResponse<Person> serviceResponse = cloudService.createPerson("Test", person);
        assertEquals(HttpURLConnection.HTTP_CREATED, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(person, serviceResponse.getData().get());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void createPerson_newTag_successfulCreationOfPersonAndTag() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        CloudPerson cloudPerson = new CloudPerson("unknownName", "unknownName");
        List<CloudTag> personCloudTags = new ArrayList<>();
        personCloudTags.add(new CloudTag("New Tag"));
        cloudPerson.setTags(personCloudTags);
        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_CREATED, cloudPerson, header);
        when(cloudSimulator.createPerson(anyString(), any(CloudPerson.class), isNull(String.class))).thenReturn(cloudResponse);

        Person person = new Person("unknownName", "unknownName");
        List<Tag> personTags = new ArrayList<>();
        personTags.add(new Tag("New Tag"));
        person.setTags(personTags);

        ExtractedCloudResponse<Person> serviceResponse = cloudService.createPerson("Test", person);
        assertEquals(HttpURLConnection.HTTP_CREATED, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        // this only checks for name equality
        assertEquals(person, serviceResponse.getData().get());
        assertEquals(person.getTags().size(), serviceResponse.getData().get().getTags().size());
        assertEquals(person.getTags().get(0).getName(), serviceResponse.getData().get().getTags().get(0).getName());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void createTag() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        CloudTag cloudTag = new CloudTag("New Tag");
        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_CREATED, cloudTag, header);
        when(cloudSimulator.createTag(anyString(), any(CloudTag.class), isNull(String.class))).thenReturn(cloudResponse);

        Tag tag = new Tag("New Tag");

        ExtractedCloudResponse<Tag> serviceResponse = cloudService.createTag("Test", tag);
        assertEquals(HttpURLConnection.HTTP_CREATED, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(tag, serviceResponse.getData().get());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void getTags() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        List<CloudTag> tagList = new ArrayList<>();
        tagList.add(new CloudTag("tagName"));

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_OK, tagList, header);
        when(cloudSimulator.getTags(anyString(), anyInt(), anyInt(), isNull(String.class))).thenReturn(cloudResponse);

        ExtractedCloudResponse<List<Tag>> serviceResponse = cloudService.getTags("Test");

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

        CloudPerson cloudPerson = new CloudPerson("newFirstName", "newLastName");
        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_OK, cloudPerson, header);
        when(cloudSimulator.updatePerson(anyString(), anyString(), anyString(), any(CloudPerson.class), isNull(String.class))).thenReturn(cloudResponse);

        Person updatedPerson = new Person("newFirstName", "newLastName");
        ExtractedCloudResponse<Person> serviceResponse = cloudService.updatePerson("Test", "firstName", "lastName", updatedPerson);

        assertEquals(HttpURLConnection.HTTP_OK, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals("newFirstName", serviceResponse.getData().get().getFirstName());
        assertEquals("newLastName", serviceResponse.getData().get().getLastName());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void editTag() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        CloudTag cloudTag = new CloudTag("newTagName");
        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_OK, cloudTag, header);
        when(cloudSimulator.editTag(anyString(), anyString(), any(CloudTag.class), isNull(String.class))).thenReturn(cloudResponse);

        Tag updatedTag = new Tag("newTagName");
        ExtractedCloudResponse<Tag> serviceResponse = cloudService.editTag("Test", "tagName", updatedTag);

        assertEquals(HttpURLConnection.HTTP_OK, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals("newTagName", serviceResponse.getData().get().getName());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void deletePerson() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_NO_CONTENT, null, header);
        when(cloudSimulator.deletePerson("Test", "firstName", "lastName")).thenReturn(cloudResponse);

        ExtractedCloudResponse<Void> serviceResponse = cloudService.deletePerson("Test", "firstName", "lastName");

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void deleteTag() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_NO_CONTENT, null, header);
        when(cloudSimulator.deleteTag("Test", "tagName")).thenReturn(cloudResponse);

        ExtractedCloudResponse<Void> serviceResponse = cloudService.deleteTag("Test", "tagName");

        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void createAddressBook() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_CREATED, null, header);
        when(cloudSimulator.createAddressBook("Test")).thenReturn(cloudResponse);

        ExtractedCloudResponse<Void> serviceResponse = cloudService.createAddressBook("Test");

        assertEquals(HttpURLConnection.HTTP_CREATED, serviceResponse.getResponseCode());
        assertFalse(serviceResponse.getData().isPresent());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void getUpdatedPersonsSince() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 9;

        LocalDateTime cutOffTime = LocalDateTime.now();
        List<CloudPerson> cloudPersons = new ArrayList<>();
        cloudPersons.add(new CloudPerson("firstName", "lastName"));

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, getResetTime());
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_OK, cloudPersons, header);
        when(cloudSimulator.getUpdatedPersons(anyString(), anyString(), anyInt(), anyInt(), anyString())).thenReturn(cloudResponse);

        ExtractedCloudResponse<List<Person>> serviceResponse = cloudService.getUpdatedPersonsSince("Test", cutOffTime);

        assertEquals(HttpURLConnection.HTTP_OK, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(1, serviceResponse.getData().get().size());
        assertEquals("firstName", serviceResponse.getData().get().get(0).getFirstName());
        assertEquals("lastName", serviceResponse.getData().get().get(0).getLastName());
        assertEquals(quotaRemaining, serviceResponse.getQuotaRemaining());
    }

    @Test
    public void getLimitStatus() throws IOException {
        int quotaLimit = 10;
        int quotaRemaining = 10;
        long resetTime = getResetTime();

        HashMap<String, String> header = getHeader(quotaLimit, quotaRemaining, resetTime);
        RawCloudResponse cloudResponse = new RawCloudResponse(HttpURLConnection.HTTP_OK, header, header);
        when(cloudSimulator.getRateLimitStatus(isNull(String.class))).thenReturn(cloudResponse);
        ExtractedCloudResponse<HashMap<String, String>> serviceResponse = cloudService.getLimitStatus();

        assertEquals(HttpURLConnection.HTTP_OK, serviceResponse.getResponseCode());
        assertTrue(serviceResponse.getData().isPresent());
        assertEquals(3, serviceResponse.getData().get().size());
        assertEquals("10", serviceResponse.getData().get().get("Limit"));
        assertEquals("10", serviceResponse.getData().get().get("Remaining"));
        assertEquals(String.valueOf(resetTime), serviceResponse.getData().get().get("Reset"));
    }
}
