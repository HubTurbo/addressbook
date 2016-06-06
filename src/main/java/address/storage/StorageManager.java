package address.storage;

import address.events.*;
import address.exceptions.FileContainsDuplicatesException;
import address.model.AddressBook;
import address.model.datatypes.Tag;
import address.model.ModelManager;
import address.model.datatypes.Person;
import address.prefs.PrefsManager;
import com.google.common.eventbus.Subscribe;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.List;

/**
 * Manages storage of addressbook data in local disk.
 * Handles storage related events.
 */
public class StorageManager {

    private ModelManager modelManager;
    private EventManager eventManager;
    private PrefsManager prefsManager;

    public StorageManager(ModelManager modelManager,
                          PrefsManager prefsManager,
                          EventManager eventManager){

        this.modelManager = modelManager;
        this.prefsManager = prefsManager;
        this.eventManager = eventManager;

        eventManager.registerHandler(this);
    }

    /**
     *  Raises a FileOpeningExceptionEvent if there was any problem in reading data from the file
     *  or if the file is not in the correct format.
     * @param ldre
     */
    @Subscribe
    private void handleLoadDataRequestEvent(LoadDataRequestEvent ldre) {
        AddressBook data;

        try {
            data = XmlFileStorage.loadDataFromSaveFile(ldre.file);
        } catch (JAXBException e) {
            e.printStackTrace();
            eventManager.post(new FileOpeningExceptionEvent(e, ldre.file));
            return;
        }

        if (data.containsDuplicates()) {
            eventManager.post(new FileOpeningExceptionEvent(new FileContainsDuplicatesException(ldre.file), ldre.file));
            return;
        }
        //TODO: move duplication detection out of this class

        modelManager.updateUsingExternalData(data);
    }

    /**
     * Raises FileSavingExceptionEvent
     * @param lmce
     */
    @Subscribe
    private void handleLocalModelChangedEvent(LocalModelChangedEvent lmce) {
        System.out.println("Local data changed, saving to primary data file");
        saveDataToFile(prefsManager.getSaveLocation(), lmce.personData, lmce.tagData);
    }

    /**
     * Raises FileSavingExceptionEvent
     * @param msfce
     */
    @Subscribe
    private void handleLocalModelSyncedFromCloudEvent(LocalModelSyncedFromCloudEvent msfce) {
        System.out.println("Local data synced, saving to primary data file");
        saveDataToFile(prefsManager.getSaveLocation(), msfce.personData, msfce.tagData);
    }

    /**
     * Raises FileSavingExceptionEvent
     * @param sre
     */
    @Subscribe
    private void handleSaveRequestEvent(SaveRequestEvent sre) {
        saveDataToFile(sre.file, sre.personData, sre.tagData);
    }

    /**
     * Raises FileSavingExceptionEvent
     * @param file
     * @param personData
     * @param tagData
     */
    private void saveDataToFile(File file, List<Person> personData, List<Tag> tagData){
        try {
            XmlFileStorage.saveDataToFile(file, new AddressBook(personData, tagData));
        } catch (JAXBException e) {
            eventManager.post(new FileSavingExceptionEvent(e, file));
        }
    }

}
