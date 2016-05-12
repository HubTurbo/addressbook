package address.storage;

import address.events.*;
import address.exceptions.FileContainsDuplicatesException;
import address.model.AddressBookWrapper;
import address.model.ContactGroup;
import address.model.ModelManager;
import address.model.Person;
import address.preferences.PreferencesManager;
import address.util.XmlHelper;
import com.google.common.eventbus.Subscribe;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.List;


public class StorageManager {

    private ModelManager modelManager;

    public StorageManager(ModelManager modelManager){
        this.modelManager = modelManager;
        EventManager.getInstance().registerHandler(this);
    }

    @Subscribe
    private void handleLoadDataRequestEvent(LoadDataRequestEvent ofe) {
        try {
            AddressBookWrapper data = loadDataFromSaveFile(ofe.file);
            PreferencesManager.getInstance().setPersonFilePath(ofe.file);
            modelManager.resetData(data);
        } catch (JAXBException | FileContainsDuplicatesException e) {
            System.out.println(e);
            EventManager.getInstance().post(new FileOpeningExceptionEvent(e, ofe.file));
        }
    }

    @Subscribe
    private void handleLocalModelChangedEvent(LocalModelChangedEvent lmce){
        final File targetFile = PreferencesManager.getInstance().getPersonFile();
        System.out.println("Local data changed, saving to primary data file");
        saveDataToFile(targetFile, lmce.personData, lmce.groupData);
    }

    @Subscribe
    private void handleLocalModelSyncedEvent(LocalModelSyncedEvent lmse){
        final File targetFile = PreferencesManager.getInstance().getPersonFile();
        System.out.println("Local data synced, saving to primary data file");
        saveDataToFile(targetFile, lmse.personData, lmse.groupData);
    }

    @Subscribe
    private void handleSaveRequestEvent(SaveRequestEvent se){
        saveDataToFile(se.file, se.personData, se.groupData);
    }

    /**
     * Saves the current person data to the specified file.
     *
     * @param file
     */
    public static void saveDataToFile(File file, List<Person> personData, List<ContactGroup> groupData) {
        try {
            XmlHelper.saveToFile(file, personData, groupData);
        } catch (Exception e) {
            EventManager.getInstance().post(new FileSavingExceptionEvent(e, file));
        }
    }

    /**
     * Raises a FileOpeningExceptionEvent if there was any problem in reading data from the file
     *  or if the file is not in the correct format.
     * @param file File containing the data
     * @return address book in the file or an empty address book if file is null
     */
    public static AddressBookWrapper loadDataFromSaveFile(File file) throws JAXBException, FileContainsDuplicatesException {
        AddressBookWrapper data = XmlHelper.getDataFromFile(file);
        if (data.containsDuplicates()) throw new FileContainsDuplicatesException();
        return data;
    }

}
