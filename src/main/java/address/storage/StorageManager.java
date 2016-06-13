package address.storage;

import address.events.*;
import address.exceptions.DataConversionException;
import address.exceptions.FileContainsDuplicatesException;
import address.main.ComponentManager;
import address.model.datatypes.AddressBook;
import address.model.ModelManager;
import address.prefs.PrefsManager;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Manages storage of addressbook data in local disk.
 * Handles storage related events.
 */
public class StorageManager extends ComponentManager {
    private static final Logger logger = LoggerManager.getLogger(StorageManager.class);

    private ModelManager modelManager;
    private PrefsManager prefsManager;

    public StorageManager(ModelManager modelManager,
                          PrefsManager prefsManager){

        super();
        this.modelManager = modelManager;
        this.prefsManager = prefsManager;
    }

    /**
     *  Raises a {@link address.events.FileOpeningExceptionEvent} if there was any problem in reading data from the file
     *  or if the file is not in the correct format.
     */
    @Subscribe
    public void handleLoadDataRequestEvent(LoadDataRequestEvent ldre) {
        AddressBook data;

        try {
            data = XmlFileStorage.loadDataFromSaveFile(ldre.file);
        } catch (FileNotFoundException | DataConversionException e) {
            e.printStackTrace();
            raise(new FileOpeningExceptionEvent(e, ldre.file));
            return;
        }

        if (data.containsDuplicates()) {
            raise(new FileOpeningExceptionEvent(new FileContainsDuplicatesException(ldre.file), ldre.file));
            return;
        }
        //TODO: move duplication detection out of this class

        modelManager.updateUsingExternalData(data);
    }

    /**
     * Raises FileSavingExceptionEvent(similar to {@link #saveDataToFile(File, AddressBook)})
     */
    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent lmce) {
        logger.info("Local data changed, saving to primary data file");
        saveDataToFile(prefsManager.getSaveLocation(), new AddressBook(lmce.personData, lmce.tagData));
    }

    /**
     * Raises FileSavingExceptionEvent(similar to {@link #saveDataToFile(File, AddressBook)})
     */
    @Subscribe
    public void handleLocalModelSyncedFromCloudEvent(LocalModelSyncedFromCloudEvent msfce) {
        logger.info("Local data synced, saving to primary data file");
        saveDataToFile(prefsManager.getSaveLocation(), new AddressBook( msfce.personData, msfce.tagData));
    }

    /**
     * Raises FileSavingExceptionEvent (similar to {@link #saveDataToFile(File, AddressBook)})
     */
    @Subscribe
    public void handleSaveRequestEvent(SaveRequestEvent sre) {
        saveDataToFile(sre.file, new AddressBook(sre.personData, sre.tagData));
    }

    /**
     * Raises FileSavingExceptionEvent if the file is not found or if there was an error during data conversion.
     */
    public void saveDataToFile(File file, AddressBook addressBook){
        try {
            XmlFileStorage.saveDataToFile(file, addressBook);
        } catch (FileNotFoundException | DataConversionException e) {
            raise(new FileSavingExceptionEvent(e, file));
        }
    }

}
