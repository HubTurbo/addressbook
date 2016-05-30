package address.storage;

import address.events.*;
import address.exceptions.FileContainsDuplicatesException;
import address.model.AddressBook;
import address.model.datatypes.Tag;
import address.model.ModelManager;
import address.model.datatypes.Person;
import address.prefs.PrefsManager;
import address.sync.model.CloudAddressBook;
import address.sync.model.CloudTag;
import address.sync.model.CloudPerson;
import address.util.XmlFileHelper;
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
            AddressBook data = loadDataFromSaveFile(ofe.file);
            modelManager.updateUsingExternalData(data);
        } catch (JAXBException | FileContainsDuplicatesException e) {
            System.out.println(e);
            EventManager.getInstance().post(new FileOpeningExceptionEvent(e, ofe.file));
        }
    }

    @Subscribe
    private void handleLocalModelChangedEvent(LocalModelChangedEvent e) {
        System.out.println("Local data changed, saving to primary data file");
        EventManager.getInstance().post(new SaveRequestEvent(
                PrefsManager.getInstance().getSaveLocation(), e.personData, e.tagData));
    }

    @Subscribe
    private void handleLocalModelSyncedFromCloudEvent(LocalModelSyncedFromCloudEvent e) {
        System.out.println("Local data synced, saving to primary data file");
        EventManager.getInstance().post(new SaveRequestEvent(
                PrefsManager.getInstance().getSaveLocation(), e.personData, e.tagData));
    }

    @Subscribe
    private void handleSaveRequestEvent(SaveRequestEvent se) {
        saveDataToFile(se.file, se.personData, se.tagData);
    }

    /**
     * Saves the current person data to the specified file.
     *
     * @param file
     */
    public static void saveDataToFile(File file, List<Person> personData, List<Tag> tagData) {
        try {
            XmlFileHelper.saveModelToFile(file, personData, tagData);
        } catch (Exception e) {
            EventManager.getInstance().post(new FileSavingExceptionEvent(e, file));
        }
    }

    /**
     * Raises a FileOpeningExceptionEvent if there was any problem in reading data from the file
     *  or if the file is not in the correct format.
     * @param file File containing the data
     * @return address book in the file or an empty address book
     */
    public static AddressBook loadDataFromSaveFile(File file) throws JAXBException, FileContainsDuplicatesException {
        assert file != null;
        AddressBook data = XmlFileHelper.getDataFromFile(file);
        if (data.containsDuplicates()) throw new FileContainsDuplicatesException(file);
        return data;
    }


    /**
     * Saves the current person cloud data to the specified file.
     *
     * @param file
     */
    public static void saveCloudDataToFile(File file, List<CloudPerson> personData, List<CloudTag> tagData) {
        try {
            XmlFileHelper.saveCloudDataToFile(file, personData, tagData);
        } catch (Exception e) {
            EventManager.getInstance().post(new FileSavingExceptionEvent(e, file));
        }
    }

    /**
     * Raises a FileOpeningExceptionEvent if there was any problem in reading data from the file
     *  or if the file is not in the correct format.
     * @param file File containing the data
     * @return address book in the file or an empty address book
     */
    public static CloudAddressBook loadCloudDataFromSaveFile(File file) throws JAXBException {
        assert file != null;
        CloudAddressBook data = XmlFileHelper.getCloudDataFromFile(file);
        return data;
    }

}
