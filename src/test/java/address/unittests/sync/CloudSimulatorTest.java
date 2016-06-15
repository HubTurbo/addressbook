package address.unittests.sync;

import address.exceptions.DataConversionException;
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

import java.io.*;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
     * @throws DataConversionException
     */
    @Before
    public void setup() throws FileNotFoundException, DataConversionException {
        final long resetTime = System.currentTimeMillis()/1000 + API_RESET_DELAY;
        cloudFileHandler = mock(CloudFileHandler.class);
        cloudRateLimitStatus = new CloudRateLimitStatus(STARTING_API_COUNT, resetTime);
        cloudSimulator = new CloudSimulator(cloudFileHandler, cloudRateLimitStatus, false);

        CloudAddressBook cloudAddressBook = getDummyAddressBook();
        stub(cloudFileHandler.readCloudAddressBookFromFile("Test")).toReturn(cloudAddressBook);

        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());
    }

    @Test
    public void createAddressBook() throws IOException, DataConversionException {
        RawCloudResponse cloudResponse = cloudSimulator.createAddressBook("Test");

        // File handler is called to create an address book file
        verify(cloudFileHandler, times(1)).createCloudAddressBookFile("Test");

        // 1 API quota is consumed
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for a successful creation
        assertEquals(HttpURLConnection.HTTP_CREATED, cloudResponse.getResponseCode());
    }

    @Test
    public void createAddressBook_notEnoughQuota_unsuccessfulCreation() throws IOException, DataConversionException {
        // Use up quota
        cloudRateLimitStatus.useQuota(STARTING_API_COUNT);
        assertEquals(0, cloudRateLimitStatus.getQuotaRemaining());

        RawCloudResponse cloudResponse = cloudSimulator.createAddressBook("Test");

        // File creation will not be called since there is no more quota
        verify(cloudFileHandler, never()).createCloudAddressBookFile("Test");

        // API count is not modified
        assertEquals(0, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for a request with insufficient quota
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, cloudResponse.getResponseCode());
    }

    @Test
    public void createAddressBook_illegalArgument_unsuccessfulCreation() throws IOException, DataConversionException {
        // Prepare filehandler to throw an exception that the addressbook already exists
        doThrow(new IllegalArgumentException("AddressBook 'Test' already exists!")).when(cloudFileHandler).createCloudAddressBookFile("Test");

        RawCloudResponse cloudResponse = cloudSimulator.createAddressBook("Test");

        // File creation method is called
        verify(cloudFileHandler, times(1)).createCloudAddressBookFile("Test");

        // Still consumes API quota since it is the caller's error
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for a bad request
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, cloudResponse.getResponseCode());
    }

    @Test
    public void createAddressBook_conversionException_unsuccessfulCreation() throws IOException, DataConversionException {
        // Prepares filehandler to throw an exception that there are problems with data conversion
        doThrow(new DataConversionException("Error in conversion when creating file.")).when(cloudFileHandler).createCloudAddressBookFile("Test");

        RawCloudResponse cloudResponse = cloudSimulator.createAddressBook("Test");

        // File creation method is called
        verify(cloudFileHandler, times(1)).createCloudAddressBookFile("Test");

        // Does not consume API quota since it is an error on the cloud's end
        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for a cloud error
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, cloudResponse.getResponseCode());
    }

    @Test
    public void deletePerson_enoughQuota_successfulDeletion() throws IOException, DataConversionException {
        RawCloudResponse cloudResponse = cloudSimulator.deletePerson("Test", 1);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is called
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // 1 API quota is used
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for deleted content
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, cloudResponse.getResponseCode());
    }

    @Test
    public void deletePerson_noSuchPerson_unsuccessfulDeletion() throws DataConversionException, FileNotFoundException {
        RawCloudResponse cloudResponse = cloudSimulator.deletePerson("Test", 2);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is never called, since there is an error
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // 1 API quota is still used, since it is the caller's error
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for a bad request
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, cloudResponse.getResponseCode());
    }

    @Test
    public void deletePerson_conversionException_unsuccessfulDeletion() throws IOException, DataConversionException {
        // Prepares filehandler to throw an exception that there are problems with data conversion
        doThrow(new DataConversionException("Error in conversion when writing to file.")).when(cloudFileHandler).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        RawCloudResponse cloudResponse = cloudSimulator.deletePerson("Test", 1);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is called
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // API quota is not consumed since it is a cloud error
        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for cloud error
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, cloudResponse.getResponseCode());
    }

    @Test
    public void updatePerson() throws DataConversionException, FileNotFoundException {
        CloudPerson updatedPerson = prepareUpdatedPerson();
        RawCloudResponse cloudResponse = cloudSimulator.updatePerson("Test", 1, updatedPerson, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is called
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // 1 API quota is consumed
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for successful update
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());
    }

    @Test
    public void updatePerson_conversionException() throws DataConversionException, FileNotFoundException {
        // Prepares filehandler to throw an exception that there are problems writing to file
        doThrow(new DataConversionException("Error in conversion when writing to file.")).when(cloudFileHandler).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        CloudPerson updatedPerson = prepareUpdatedPerson();
        RawCloudResponse cloudResponse = cloudSimulator.updatePerson("Test", 1, updatedPerson, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is called
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // API quota is not consumed since it is a cloud error
        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for cloud error
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, cloudResponse.getResponseCode());
    }

    @Test
    public void updatePerson_noSuchPerson() throws DataConversionException, FileNotFoundException {
        CloudPerson updatedPerson = prepareUpdatedPerson();
        RawCloudResponse cloudResponse = cloudSimulator.updatePerson("Test", 2, updatedPerson, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is never called, since there is an error
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // API quota is still consumed, since it is the caller's error
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for bad request
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, cloudResponse.getResponseCode());
    }

    @Test
    public void updatePerson_notEnoughQuota_unsuccessfulUpdate() throws DataConversionException, FileNotFoundException {
        // Use up quota
        cloudRateLimitStatus.useQuota(STARTING_API_COUNT);
        assertEquals(0, cloudRateLimitStatus.getQuotaRemaining());

        CloudPerson updatedPerson = prepareUpdatedPerson();
        RawCloudResponse cloudResponse = cloudSimulator.updatePerson("Test", 1, updatedPerson, null);

        // File read is not called, since there is no quota
        verify(cloudFileHandler, never()).readCloudAddressBookFromFile("Test");

        // File write is not called, since there is no quota
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // API quota remaining does not change
        assertEquals(0, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for insufficient quota
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, cloudResponse.getResponseCode());
    }

    @Test
    public void updatePerson_noSuchTag_successfulUpdateAndAddedTag() throws DataConversionException, FileNotFoundException {
        // Updated person with a new tag not previously defined
        CloudPerson updatedPerson = prepareUpdatedPerson();
        CloudTag newTag = new CloudTag("New Tag");
        List<CloudTag> tagList = new ArrayList<>();
        tagList.add(newTag);
        updatedPerson.setTags(tagList);

        // Expected result is that the person should be updated as it is
        // And the new tag should be added to the list of tags
        List<CloudPerson> updatedPersonList = new ArrayList<>();
        updatedPersonList.add(updatedPerson);
        List<CloudTag> updatedTagList = new ArrayList<>();
        updatedTagList.add(new CloudTag("Tag one"));
        updatedTagList.add(newTag);
        CloudAddressBook updatedAddressBook = new CloudAddressBook("Test", updatedPersonList, updatedTagList);

        RawCloudResponse cloudResponse = cloudSimulator.updatePerson("Test", 1, updatedPerson, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is called, with the expected result
        updatedPerson.setId(1);
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(updatedAddressBook);

        // API quota is consumed
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for successful update
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());
    }

    @Test
    public void createTag() throws DataConversionException, IOException {
        // Tag to be created
        CloudTag newTag = new CloudTag("New Tag");

        // Expected result after execution
        CloudAddressBook updatedCloudAddressBook = getDummyAddressBook();
        updatedCloudAddressBook.getAllTags().add(newTag);

        RawCloudResponse cloudResponse = cloudSimulator.createTag("Test", newTag, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is called, with expected result
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(updatedCloudAddressBook);

        // API quota is used
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for successful creation
        assertEquals(HttpURLConnection.HTTP_CREATED, cloudResponse.getResponseCode());

        // Returned content should be the same
        CloudTag cloudTag = JsonUtil.fromJsonString(convertToString(cloudResponse.getBody()), CloudTag.class);
        assertEquals(newTag, cloudTag);
    }

    @Test
    public void createTag_conversionException() throws DataConversionException, IOException {
        // Tag to be created
        CloudTag newTag = new CloudTag("New Tag");

        // Expected result after adding tag
        CloudAddressBook updatedCloudAddressBook = getDummyAddressBook();
        updatedCloudAddressBook.getAllTags().add(newTag);

        // However exception is thrown when writing to file
        doThrow(new DataConversionException("Error in conversion when writing to file.")).when(cloudFileHandler).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        RawCloudResponse cloudResponse = cloudSimulator.createTag("Test", newTag, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is called, with expected result
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(updatedCloudAddressBook);

        // API quota is not consumed, since it is a cloud error
        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());

        // Response data should be null
        assertNull(cloudResponse.getBody());

        // Response code for cloud error
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, cloudResponse.getResponseCode());
    }

    @Test
    public void createTag_alreadyExists() throws DataConversionException, IOException {
        // Tag to be created
        CloudTag newTag = new CloudTag("Tag one");

        // Expected result after adding tag
        CloudAddressBook updatedCloudAddressBook = getDummyAddressBook();
        updatedCloudAddressBook.getAllTags().add(newTag);

        RawCloudResponse cloudResponse = cloudSimulator.createTag("Test", newTag, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is never called, since there is an error
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(updatedCloudAddressBook);

        // API quota is still consumed, since it is the caller's error
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response body should be null
        assertNull(cloudResponse.getBody());

        // Response code for a bad request
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, cloudResponse.getResponseCode());
    }

    @Test
    public void editTag() throws DataConversionException, FileNotFoundException {
        // Updated tag to use
        CloudTag updatedTag = new CloudTag("Updated tag");

        // Expected result after updating tag
        CloudAddressBook updatedCloudAddressBook = getDummyAddressBook();
        updatedCloudAddressBook.getAllTags().remove(new CloudTag("Tag one"));
        updatedCloudAddressBook.getAllTags().add(updatedTag);

        RawCloudResponse cloudResponse = cloudSimulator.editTag("Test", "Tag one", updatedTag, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is called
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(updatedCloudAddressBook);

        // API quota is consumed
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for successful update
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());
    }

    @Test
    public void editTag_conversionException() throws DataConversionException, FileNotFoundException {
        CloudTag updatedTag = new CloudTag("Updated tag");
        doThrow(new DataConversionException("Error in conversion when writing to file.")).when(cloudFileHandler).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        CloudAddressBook updatedCloudAddressBook = getDummyAddressBook();
        updatedCloudAddressBook.getAllTags().remove(new CloudTag("Tag one"));
        updatedCloudAddressBook.getAllTags().add(updatedTag);

        RawCloudResponse cloudResponse = cloudSimulator.editTag("Test", "Tag one", updatedTag, null);
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(updatedCloudAddressBook);
        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, cloudResponse.getResponseCode());
    }

    @Test
    public void editTag_noSuchTag() throws DataConversionException, FileNotFoundException {
        // Updated tag to use
        CloudTag updatedTag = new CloudTag("Updated tag");

        RawCloudResponse cloudResponse = cloudSimulator.editTag("Test", "Tag two", updatedTag, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is not called, since there is an error
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // API quota is still consumed, since it is the caller's fault
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for a bad request
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, cloudResponse.getResponseCode());
    }

    @Test
    public void deleteTag() throws DataConversionException, FileNotFoundException {
        // Expected result after deleting tag
        CloudAddressBook resultingAddressBook = getDummyAddressBook();
        resultingAddressBook.getAllTags().remove(new CloudTag("Tag one"));

        RawCloudResponse cloudResponse = cloudSimulator.deleteTag("Test", "Tag one");

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is called
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(resultingAddressBook);

        // API quota is consumed
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for successful deletion
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, cloudResponse.getResponseCode());
    }

    @Test
    public void deleteTag_conversionException() throws DataConversionException, FileNotFoundException {
        // Expected result after deleting tag
        CloudAddressBook resultingAddressBook = getDummyAddressBook();
        resultingAddressBook.getAllTags().remove(new CloudTag("Tag one"));

        // Prepare filehandler to throw an exception that there are problems with data conversion
        doThrow(new DataConversionException("Exception in conversion when writing to file.")).when(cloudFileHandler).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        RawCloudResponse cloudResponse = cloudSimulator.deleteTag("Test", "Tag one");

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is called
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(resultingAddressBook);

        // API is not used, since it is a cloud error
        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for a cloud error
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, cloudResponse.getResponseCode());
    }

    @Test
    public void deleteTag_noSuchTag() throws DataConversionException, FileNotFoundException {
        RawCloudResponse cloudResponse = cloudSimulator.deleteTag("Test", "Tag two");

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is never called, since there is an error
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // API quota is used, since it is the caller's fault
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Repsonse code for a bad request
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, cloudResponse.getResponseCode());
    }

    @Test
    public void getTags() throws DataConversionException, IOException {
        final int pageNumber = 11;
        final int resourcesPerPage = 20;

        // Overwrite default addressbook response
        CloudAddressBook bigCloudAddressBook = getBigDummyAddressBook();
        stub(cloudFileHandler.readCloudAddressBookFromFile("Big Test")).toReturn(bigCloudAddressBook);

        RawCloudResponse cloudResponse = cloudSimulator.getTags("Big Test", pageNumber, resourcesPerPage, null);

        // File read is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Big Test");

        // File write is never called, since there is nothing to write
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(bigCloudAddressBook);

        // API quota is consumsed
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for successful get
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());

        // Expected result
        List<CloudTag> tagList = JsonUtil.fromJsonStringToList(convertToString(cloudResponse.getBody()), CloudTag.class);

        // Correct number of resources retrieved
        assertEquals(resourcesPerPage, tagList.size());

        // Correct tags are retrieved
        for (int i = (pageNumber - 1) * resourcesPerPage; i < pageNumber * resourcesPerPage; i++) {
            assertTrue(tagList.contains(new CloudTag("Tag" + i)));
        }

        // There is a next & prev page numbers, since we are retrieving
        // from near the middle of the list of tags
        assertEquals(pageNumber + 1, cloudResponse.getNextPageNo());
        assertEquals(pageNumber - 1, cloudResponse.getPreviousPageNo());

        // First page is always 1, last page number is dependent on the number of resources/resourcePerPage
        assertEquals(1, cloudResponse.getFirstPageNo());
        assertEquals((int) Math.ceil(1000/resourcesPerPage), cloudResponse.getLastPageNo());
    }

    @Test
    public void getPersons() throws DataConversionException, IOException {
        final int pageNumber = 12;
        final int resourcesPerPage = 30;

        // Overwrite default address book
        CloudAddressBook bigCloudAddressBook = getBigDummyAddressBook();
        stub(cloudFileHandler.readCloudAddressBookFromFile("Big Test")).toReturn(bigCloudAddressBook);

        RawCloudResponse cloudResponse = cloudSimulator.getPersons("Big Test", pageNumber, resourcesPerPage, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Big Test");

        // File write method is never called, since there is nothing to write
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(bigCloudAddressBook);

        // API quota is consumed
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for successful get
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());

        // Expected result
        List<CloudPerson> personList = JsonUtil.fromJsonStringToList(convertToString(cloudResponse.getBody()), CloudPerson.class);

        // Correct number of resources retrieved
        assertEquals(resourcesPerPage, personList.size());

        // Correct persons are retrieved
        for (int i = (pageNumber - 1) * resourcesPerPage; i < pageNumber * resourcesPerPage; i++) {
            assertTrue(personList.contains(new CloudPerson("firstName" + i, "lastName" + i)));
        }

        // Prev/next page numbers are defined
        assertEquals(pageNumber + 1, cloudResponse.getNextPageNo());
        assertEquals(pageNumber - 1, cloudResponse.getPreviousPageNo());

        // First page is always 1, and last page is always resources/resourcesPerPage.
        assertEquals(1, cloudResponse.getFirstPageNo());
        assertEquals((int) Math.ceil(2000/resourcesPerPage), cloudResponse.getLastPageNo());
    }

    @Test
    public void getPersons_sameRequest_notModifiedResponse() throws DataConversionException, FileNotFoundException {
        final int pageNumber = 12;
        final int resourcesPerPage = 30;

        // Overwrite default address book
        CloudAddressBook bigCloudAddressBook = getBigDummyAddressBook();
        stub(cloudFileHandler.readCloudAddressBookFromFile("Big Test")).toReturn(bigCloudAddressBook).toReturn(bigCloudAddressBook);

        RawCloudResponse cloudResponse = cloudSimulator.getPersons("Big Test", pageNumber, resourcesPerPage, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Big Test");

        // File write method is never called, since there is nothing to write
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(bigCloudAddressBook);

        // API quota is used
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for successful get
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());

        // Extract the previous response's ETag
        String responseETag = cloudResponse.getHeaders().get("ETag");

        // Call the same method with extracted ETag
        RawCloudResponse cloudResponse2 = cloudSimulator.getPersons("Big Test", pageNumber, resourcesPerPage, responseETag);

        // File read method has been called twice
        verify(cloudFileHandler, times(2)).readCloudAddressBookFromFile("Big Test");

        // File write method still not called, since there is nothing to write
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(bigCloudAddressBook);

        // API quota is NOT consumsed, since the cloud recognises it as a repeated request (using supplied ETag)
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for no modification
        assertEquals(HttpURLConnection.HTTP_NOT_MODIFIED, cloudResponse2.getResponseCode());
    }

    @Test
    public void getRateLimitStatus() throws DataConversionException, FileNotFoundException {
        RawCloudResponse cloudResponse = cloudSimulator.getRateLimitStatus(null);
        verify(cloudFileHandler, never()).readCloudAddressBookFromFile("Big Test");
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));
        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());
        assertEquals(HttpURLConnection.HTTP_OK, cloudResponse.getResponseCode());
    }

    @Test
    public void getUpdatedPersons() throws DataConversionException, IOException {
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
    public void getUpdatedPersons_conversionException() throws DataConversionException, IOException {
        final int pageNumber = 1;
        final int resourcesPerPage = 30;

        // Overwrite default address book
        CloudAddressBook bigCloudAddressBook = getBigDummyAddressBook();
        stub(cloudFileHandler.readCloudAddressBookFromFile("Big Test")).toReturn(bigCloudAddressBook);

        // Save current time before moving on to update data/used for the next call
        String cutOffTime = LocalDateTime.now().toString();

        // Update a person
        CloudPerson updatedPerson = new CloudPerson("firstName353", "lastName353");
        bigCloudAddressBook.getAllPersons().get(352).updatedBy(updatedPerson);

        // Prepare to throw exception that there is an error during data conversion
        doThrow(new DataConversionException("Error in conversion when reading file.")).when(cloudFileHandler).readCloudAddressBookFromFile("Big Test");

        RawCloudResponse cloudResponse = cloudSimulator.getUpdatedPersons("Big Test", cutOffTime, pageNumber, resourcesPerPage, null);

        // File read method called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Big Test");

        // File write method never called, since there is nothing to write
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // API quota is not consumed, since it is a cloud error
        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for a cloud error
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, cloudResponse.getResponseCode());

        // Body is null
        assertNull(cloudResponse.getBody());

        // No pages numbers have been set
        assertEquals(-1, cloudResponse.getNextPageNo());
        assertEquals(-1, cloudResponse.getPreviousPageNo());
        assertEquals(-1, cloudResponse.getFirstPageNo());
        assertEquals(-1, cloudResponse.getLastPageNo());
    }

    @Test
    public void createPerson() throws DataConversionException, IOException {
        CloudPerson cloudPerson = new CloudPerson("unknownName", "unknownName");

        RawCloudResponse cloudResponse = cloudSimulator.createPerson("Test", cloudPerson, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is called
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // API quota is consumed
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Response code for successful creation
        assertEquals(HttpURLConnection.HTTP_CREATED, cloudResponse.getResponseCode());

        CloudPerson person = JsonUtil.fromJsonString(convertToString(cloudResponse.getBody()), CloudPerson.class);

        // Resulting data should be the same
        assertEquals(cloudPerson, person);
    }

    @Test
    public void createPerson_alreadyExists_unsuccessfulCreation() throws DataConversionException, IOException {
        CloudPerson cloudPerson = new CloudPerson("firstName", "lastName");
        RawCloudResponse cloudResponse = cloudSimulator.createPerson("Test", cloudPerson, null);

        // File read method called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method never called, since there is an error
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // Response code for a bad request
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Body is null
        assertNull(cloudResponse.getBody());

        // Response code for a bad request
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, cloudResponse.getResponseCode());
    }

    @Test
    public void createPerson_nullArgument_unsuccessfulCreation() throws DataConversionException, IOException {
        RawCloudResponse cloudResponse = cloudSimulator.createPerson("Test", null, null);

        // File read method called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method not called, since there is an error
        verify(cloudFileHandler, never()).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // API quota is consumed
        assertEquals(STARTING_API_COUNT - 1, cloudRateLimitStatus.getQuotaRemaining());

        // Body is null
        assertNull(cloudResponse.getBody());

        // Response code for a bad request
        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, cloudResponse.getResponseCode());
    }

    @Test
    public void createPerson_conversionException_unsuccessfulCreation() throws DataConversionException, IOException {
        // Prepare to throw an exception that the data is compatible
        doThrow(new DataConversionException("Error in conversion.")).when(cloudFileHandler).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        CloudPerson cloudPerson = new CloudPerson("unknownName", "unknownName");
        RawCloudResponse cloudResponse = cloudSimulator.createPerson("Test", cloudPerson, null);

        // File read method is called
        verify(cloudFileHandler, times(1)).readCloudAddressBookFromFile("Test");

        // File write method is called
        verify(cloudFileHandler, times(1)).writeCloudAddressBookToFile(any(CloudAddressBook.class));

        // API is not used, since it is the server's error
        assertEquals(STARTING_API_COUNT, cloudRateLimitStatus.getQuotaRemaining());

        // Should be null
        assertNull(cloudResponse.getBody());

        // Response code for a cloud error
        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, cloudResponse.getResponseCode());
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

        CloudPerson dummyPerson = new CloudPerson("firstName", "lastName");
        dummyPerson.setId(1);
        cloudAddressBook.getAllPersons().add(dummyPerson);

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
