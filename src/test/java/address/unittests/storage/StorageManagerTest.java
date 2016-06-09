package address.unittests.storage;

import address.events.*;
import address.exceptions.DataConversionException;
import address.model.AddressBook;
import address.model.ModelManager;
import address.prefs.PrefsManager;
import address.storage.StorageManager;
import address.storage.XmlFileStorage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileNotFoundException;

import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest(XmlFileStorage.class)
public class StorageManagerTest {

    private static final File DUMMY_FILE = new File("dummy");
    private static final AddressBook EMPTY_ADDRESSBOOK = new AddressBook();
    ModelManager modelManagerMock;
    EventManager eventManagerMock;
    PrefsManager prefsManagerMock;
    StorageManager storageManager;
    StorageManager storageManagerSpy;

    @Before
    public void setup(){

        //mock the dependent static class
        PowerMockito.mockStatic(XmlFileStorage.class);

        //create mocks for dependencies and inject them into StorageManager object under test
        modelManagerMock = Mockito.mock(ModelManager.class);
        eventManagerMock = Mockito.mock(EventManager.class);
        prefsManagerMock = Mockito.mock(PrefsManager.class);
        storageManager = new StorageManager(modelManagerMock, prefsManagerMock);
        storageManager.setEventManager(eventManagerMock);

        // This spy will be used to mock only one method of the object under test
        storageManagerSpy = spy(storageManager);
        doNothing().when(storageManagerSpy).saveDataToFile(DUMMY_FILE,EMPTY_ADDRESSBOOK);
    }

    @Test
    public void saveDataToFile_valid_noEvents() throws DataConversionException, FileNotFoundException {

        //invoke method under test
        storageManager.saveDataToFile(DUMMY_FILE,EMPTY_ADDRESSBOOK);

        //verify the dependent method was called correctly
        PowerMockito.verifyStatic();
        XmlFileStorage.saveDataToFile(DUMMY_FILE, EMPTY_ADDRESSBOOK);
    }

    @Test
    public void saveDataToFile_fileNotFound_exceptionEvenRaised() throws DataConversionException, FileNotFoundException {
        verifyExceptionEventRaised(new FileNotFoundException("dummy file not found error"));
    }

    @Test
    public void saveDataToFile_dataConversionError_exceptionEvenRaised() throws DataConversionException, FileNotFoundException {
        verifyExceptionEventRaised(new DataConversionException(new Exception("dummy data conversion error")));
    }

    /**
     * Verifies the given FileSavingExceptionEvent is raised when the dependent method throws the given exception.
     * @param exceptionToExpect The exception that will be thrown by the dependent method.
     */
    private void verifyExceptionEventRaised(Exception exceptionToExpect) throws DataConversionException, FileNotFoundException {
        // set up to throw exception from the collaborating method XmlFileStorage.saveDataToFile
        PowerMockito.doThrow(exceptionToExpect).when(XmlFileStorage.class);
        XmlFileStorage.saveDataToFile(DUMMY_FILE, EMPTY_ADDRESSBOOK);

        //invoke the method under test
        storageManager.saveDataToFile(DUMMY_FILE,EMPTY_ADDRESSBOOK);

        //verify the relevant event was raised
        verify(eventManagerMock, times(1)).post(Mockito.any(FileSavingExceptionEvent.class));
    }

    @Test
    public void handleSaveRequestEvent(){

        //mock dependent method of same object (that method is tested elsewhere)
        storageManagerSpy.handleSaveRequestEvent(
                new SaveRequestEvent(DUMMY_FILE,EMPTY_ADDRESSBOOK.getPersons(),EMPTY_ADDRESSBOOK.getTags()));

        //verify that method is called correctly
        verify(storageManagerSpy, times(1)).saveDataToFile(any(File.class), any(AddressBook.class));
        //TODO: make the above verification stronger by comparing actual parameters instead of 'any'
    }

    @Test
    public void handleLocalModelSyncedFromCloudEvent(){

        //mock dependent method of same object (that method is tested elsewhere)
        storageManagerSpy.handleLocalModelSyncedFromCloudEvent(
                new LocalModelSyncedFromCloudEvent(EMPTY_ADDRESSBOOK.getPersons(),EMPTY_ADDRESSBOOK.getTags()));

        //verify that method is called correctly
        verify(storageManagerSpy, times(1)).saveDataToFile(any(File.class), any(AddressBook.class));
        //TODO: make the above verification stronger by comparing actual parameters instead of 'any'
    }

    @Test
    public void handleLocalModelChangedEvent(){

        //mock dependent method of same object (that method is tested elsewhere)
        storageManagerSpy.handleLocalModelChangedEvent(
                new LocalModelChangedEvent(EMPTY_ADDRESSBOOK.getPersons(),EMPTY_ADDRESSBOOK.getTags()));

        //verify that method is called correctly
        verify(storageManagerSpy, times(1)).saveDataToFile(any(File.class), any(AddressBook.class));
        //TODO: make the above verification stronger by comparing actual parameters instead of 'any'
    }

    @Test
    public void handleLoadDataRequestEvent_noError_noEventRaised() throws FileNotFoundException, DataConversionException {

        //set up response from dependent method
        PowerMockito.when(XmlFileStorage.loadDataFromSaveFile(DUMMY_FILE)).thenReturn(EMPTY_ADDRESSBOOK);

        //invoke method under test
        storageManager.handleLoadDataRequestEvent(new LoadDataRequestEvent(DUMMY_FILE));

        //verify the dependent method was called
        PowerMockito.verifyStatic();
        XmlFileStorage.loadDataFromSaveFile(DUMMY_FILE);

        //verify modelManager was updated with correct data
        verify(modelManagerMock, times(1)).updateUsingExternalData(EMPTY_ADDRESSBOOK);
    }

    @Test
    public void handleLoadDataRequestEvent_fileNotFound_exceptionEventRaised()
            throws FileNotFoundException, DataConversionException {

        //set up to throw exception from dependent method
        PowerMockito.when(XmlFileStorage.loadDataFromSaveFile(DUMMY_FILE))
                .thenThrow(new FileNotFoundException("dummy exception"));

        //invoke method under test
        storageManager.handleLoadDataRequestEvent(new LoadDataRequestEvent(DUMMY_FILE));

        //verify the relevant event was raised
        verify(eventManagerMock, times(1)).post(Mockito.any(FileOpeningExceptionEvent.class));
    }

    @Test
    public void handleLoadDataRequestEvent_dataConversionError_exceptionEventRaised()
            throws FileNotFoundException, DataConversionException {

        //set up to throw exception from dependent method
        PowerMockito.when(XmlFileStorage.loadDataFromSaveFile(DUMMY_FILE))
                .thenThrow(new DataConversionException(new Exception("dummy exception")));

        //invoke method under test
        storageManager.handleLoadDataRequestEvent(new LoadDataRequestEvent(DUMMY_FILE));

        //verify the relevant event was raised
        verify(eventManagerMock, times(1)).post(Mockito.any(FileOpeningExceptionEvent.class));
    }

}
