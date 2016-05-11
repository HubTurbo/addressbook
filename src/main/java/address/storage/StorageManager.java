package address.storage;

import address.events.*;
import address.model.AddressBookWrapper;
import address.model.ContactGroup;
import address.model.ModelManager;
import address.model.Person;
import address.preferences.PreferencesManager;
import address.util.XmlHelper;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.util.List;



public class StorageManager {

    private ModelManager modelManager;

    public StorageManager(ModelManager modelManager){
        EventManager.getInstance().registerHandler(this);
    }


    /**
     * Loads person data from the specified file. The current person data will
     * be replaced.
     *
     */
    public void loadPersonDataFromFile(File file) throws Exception {
        AddressBookWrapper data  = XmlHelper.getDataFromFile(file);

        modelManager.resetData(data.getPersons(), data.getGroups());

        // Save the file path to the registry.
        PreferencesManager.getInstance().setPersonFilePath(file);
    }



    @Subscribe
    private void handleLoadDataRequestEvent(LoadDataRequestEvent ofe) {
        try {
            loadPersonDataFromFile(ofe.file);
        } catch (Exception e) {
            EventManager.getInstance().post(new FileOpeningExceptionEvent(e,ofe.file));
        }
    }

    /**
     * Saves the current person data to the specified file.
     *
     * @param file
     */
    public void saveDataToFile(File file, List<Person> personData, List<ContactGroup> groupData) {
        try {
            XmlHelper.saveToFile(file, personData, groupData);

            // Save the file path to the registry.
            PreferencesManager.getInstance().setPersonFilePath(file);

        } catch (Exception e) {
            EventManager.getInstance().post(new FileSavingExceptionEvent(e,file));
        }
    }

    @Subscribe
    private void handleLocalModelChangedEvent(LocalModelChangedEvent lmce){
        System.out.println("Local data changed, saving to primary data file");
        saveDataToFile(PreferencesManager.getInstance().getPersonFile(), lmce.personData, lmce.groupData);
    }

    @Subscribe
    private void handleLocalModelSyncedEvent(LocalModelSyncedEvent lmse){
        System.out.println("Local data synced, saving to primary data file");
        saveDataToFile(PreferencesManager.getInstance().getPersonFile(), lmse.personData, lmse.groupData);
    }

    @Subscribe
    private void handleSaveRequestEvent(SaveRequestEvent se){
        saveDataToFile(se.file, se.personData, se.groupData);
    }

    /**
     * Raises a FileOpeningExceptionEvent if there was any problem in reading data from the file
     *  or if the file is not in the correct format.
     * @param file File containing the data
     * @return Person list in the file
     */
    public static AddressBookWrapper getDataFromFile(File file)  {
        try {
            return file == null ? null : XmlHelper.getDataFromFile(file);
        } catch (Exception e) {
            EventManager.getInstance().post(new FileOpeningExceptionEvent(e, file));
            return new AddressBookWrapper();
        }
    }

}
