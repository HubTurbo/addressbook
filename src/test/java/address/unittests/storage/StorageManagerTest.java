package address.unittests.storage;

import address.events.*;
import address.exceptions.DataConversionException;
import address.model.datatypes.AddressBook;
import address.model.ModelManager;
import address.storage.StorageAddressBook;
import address.model.UserPrefs;
import address.storage.StorageManager;
import address.storage.XmlFileStorage;
import address.util.Config;
import address.util.TestUtil;
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

    private static final File DUMMY_DATA_FILE = new File(TestUtil.appendToSandboxPath("dummyAddressBook.xml"));
    private static final File DUMMY_PREFS_FILE = new File(TestUtil.appendToSandboxPath("dummyUserPrefs.json"));
    private static final StorageAddressBook EMPTY_ADDRESSBOOK = new StorageAddressBook(new AddressBook());
    private static final UserPrefs EMPTY_USERPREFS = new UserPrefs();
    ModelManager modelManagerMock;
    EventManager eventManagerMock;
    Config configMock;
    UserPrefs userPrefsMock;
    StorageManager storageManager;
    StorageManager storageManagerSpy;

    @Before
    public void setup(){

        //mock the dependent static class
        PowerMockito.mockStatic(XmlFileStorage.class);

        //create mocks for dependencies and inject them into StorageManager object under test
        userPrefsMock = Mockito.mock(UserPrefs.class);
        when(userPrefsMock.getSaveLocation()).thenReturn(DUMMY_DATA_FILE);
        modelManagerMock = Mockito.mock(ModelManager.class);
        when(modelManagerMock.getPrefs()).thenReturn(userPrefsMock);
        eventManagerMock = Mockito.mock(EventManager.class);
        configMock = Mockito.mock(Config.class);
        when(configMock.getPrefsFileLocation()).thenReturn(DUMMY_PREFS_FILE);
        storageManager = new StorageManager(modelManagerMock::resetData, configMock, userPrefsMock);
        storageManager.setEventManager(eventManagerMock);

        // This spy will be used to mock only one method of the object under test
        storageManagerSpy = spy(storageManager);
        doNothing().when(storageManagerSpy).saveDataToFile(DUMMY_DATA_FILE,EMPTY_ADDRESSBOOK);
    }

    @Test
    public void handleSaveDataRequestEvent(){

        //mock dependent method of same object (that method is tested elsewhere)
        storageManagerSpy.handleSaveDataRequestEvent(
                new SaveDataRequestEvent(DUMMY_DATA_FILE, EMPTY_ADDRESSBOOK));

        //verify that method is called correctly
        verify(storageManagerSpy, times(1)).saveDataToFile(any(File.class), any(AddressBook.class));
        //TODO: make the above verification stronger by comparing actual parameters instead of 'any'
    }

    @Test
    public void handleLocalModelChangedEvent(){

        //mock dependent method of same object (that method is tested elsewhere)
        storageManagerSpy.handleLocalModelChangedEvent(new LocalModelChangedEvent(EMPTY_ADDRESSBOOK));

        //verify that method is called correctly
        verify(storageManagerSpy, times(1)).saveDataToFile(any(File.class), any(AddressBook.class));
        //TODO: make the above verification stronger by comparing actual parameters instead of 'any'
    }

    @Test
    public void handleLoadDataRequestEvent_noError_noEventRaised() throws FileNotFoundException, DataConversionException {

        //set up response from dependent method
        PowerMockito.when(XmlFileStorage.loadDataFromSaveFile(DUMMY_DATA_FILE)).thenReturn(EMPTY_ADDRESSBOOK);

        //invoke method under test
        storageManager.handleLoadDataRequestEvent(new LoadDataRequestEvent(DUMMY_DATA_FILE));

        //verify the dependent method was called
        PowerMockito.verifyStatic();
        XmlFileStorage.loadDataFromSaveFile(DUMMY_DATA_FILE);

        //verify modelManager was updated with correct data
        verify(modelManagerMock, times(1)).resetData(EMPTY_ADDRESSBOOK);
    }

    @Test
    public void handleLoadDataRequestEvent_fileNotFound_exceptionEventRaised()
            throws FileNotFoundException, DataConversionException {

        //set up to throw exception from dependent method
        PowerMockito.when(XmlFileStorage.loadDataFromSaveFile(DUMMY_DATA_FILE))
                .thenThrow(new FileNotFoundException("dummy exception"));

        //invoke method under test
        storageManager.handleLoadDataRequestEvent(new LoadDataRequestEvent(DUMMY_DATA_FILE));

        //verify the relevant event was raised
        verify(eventManagerMock, times(1)).post(Mockito.any(FileOpeningExceptionEvent.class));
    }

    @Test
    public void handleLoadDataRequestEvent_dataConversionError_exceptionEventRaised()
            throws FileNotFoundException, DataConversionException {

        //set up to throw exception from dependent method
        PowerMockito.when(XmlFileStorage.loadDataFromSaveFile(DUMMY_DATA_FILE))
                .thenThrow(new DataConversionException(new Exception("dummy exception")));

        //invoke method under test
        storageManager.handleLoadDataRequestEvent(new LoadDataRequestEvent(DUMMY_DATA_FILE));

        //verify the relevant event was raised
        verify(eventManagerMock, times(1)).post(Mockito.any(FileOpeningExceptionEvent.class));
    }

    @Test
    public void handleSavePrefsRequestEvent(){

        //mock dependent method of same object (that method is tested elsewhere)
        storageManagerSpy.handleSavePrefsRequestEvent(
                new SavePrefsRequestEvent(EMPTY_USERPREFS));

        //verify that method is called correctly
        verify(storageManagerSpy, times(1)).savePrefsToFile(EMPTY_USERPREFS);
    }
}
