package address.storage;

import address.events.*;
import address.exceptions.DataConversionException;
import address.exceptions.FileContainsDuplicatesException;
import address.main.ComponentManager;
import address.model.datatypes.AddressBook;
import address.model.ModelManager;
import address.model.datatypes.ReadOnlyAddressBook;
import address.prefs.PrefsManager;
import address.util.AppLogger;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Manages storage of addressbook data in local disk.
 * Handles storage related events.
 */
public class StorageManager extends ComponentManager {
    private static final AppLogger logger = LoggerManager.getLogger(StorageManager.class);

    private ModelManager modelManager;
    private PrefsManager prefsManager;

    public StorageManager(ModelManager modelManager,
                          PrefsManager prefsManager) {

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
        logger.info("Load data request received: " + ldre.file);
        try {
            logger.debug("Attempting to load data from file: " + ldre.file);
            modelManager.updateUsingExternalData(XmlFileStorage.loadDataFromSaveFile(ldre.file));
        } catch (FileNotFoundException | DataConversionException e) {
            logger.debug("Error loading data from file: {}", e);
            raise(new FileOpeningExceptionEvent(e, ldre.file));
        }
    }

    /**
     * Raises FileSavingExceptionEvent(similar to {@link #saveDataToFile(File, ReadOnlyAddressBook)})
     */
    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent lmce) {
        logger.info("Local data changed, saving to primary data file");
        saveDataToFile(prefsManager.getSaveLocation(), lmce.data);
    }

    /**
     * Raises FileSavingExceptionEvent (similar to {@link #saveDataToFile(File, ReadOnlyAddressBook)})
     */
    @Subscribe
    public void handleSaveRequestEvent(SaveRequestEvent sre) {
        logger.info("Save data request received: ", sre.data);
        saveDataToFile(sre.file, sre.data);
    }

    /**
     * Raises FileSavingExceptionEvent if the file is not found or if there was an error during data conversion.
     */
    public void saveDataToFile(File file, ReadOnlyAddressBook data){
        try {
            XmlFileStorage.saveDataToFile(file, new StorageAddressBook(data));
        } catch (FileNotFoundException | DataConversionException e) {
            raise(new FileSavingExceptionEvent(e, file));
        }
    }

}
