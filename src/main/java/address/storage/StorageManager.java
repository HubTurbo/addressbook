package address.storage;

import address.events.*;
import address.exceptions.DataConversionException;
import address.main.ComponentManager;
import address.model.ModelManager;
import address.model.datatypes.ReadOnlyAddressBook;
import address.prefs.UserPrefs;
import address.util.AppLogger;
import address.util.FileUtil;
import address.util.LoggerManager;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Manages storage of addressbook data in local disk.
 * Handles storage related events.
 */
public class StorageManager extends ComponentManager {
    private static final AppLogger logger = LoggerManager.getLogger(StorageManager.class);

    private ModelManager modelManager;
    private UserPrefs userPrefs;

    public StorageManager(ModelManager modelManager,
                          UserPrefs userPrefs) {

        super();
        this.modelManager = modelManager;
        this.userPrefs = userPrefs;
    }

    /**
     *  Raises a {@link address.events.FileOpeningExceptionEvent} if there was any problem in reading data from the file
     *  or if the file is not in the correct format.
     */
    @Subscribe
    public void handleLoadDataRequestEvent(LoadDataRequestEvent ldre) {
        File dataFile = ldre.file;
        logger.info("Handling load data request received: " + dataFile);
        loadDataFromFile(dataFile);
    }

    protected void loadDataFromFile(File dataFile) {
        try {
            logger.debug("Attempting to load data from file: " + dataFile);
            modelManager.updateUsingExternalData(XmlFileStorage.loadDataFromSaveFile(dataFile));
        } catch (FileNotFoundException | DataConversionException e) {
            logger.debug("Error loading data from file: {}", e);
            raise(new FileOpeningExceptionEvent(e, dataFile));
        }
    }

    /**
     * Raises FileSavingExceptionEvent (similar to {@link #saveDataToFile(File, ReadOnlyAddressBook)})
     */
    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent lmce) {
        logger.info("Local data changed, saving to primary data file");
        saveDataToFile(userPrefs.getSaveLocation(), lmce.data);
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
     * Creates the file if it is missing before saving.
     * Raises FileSavingExceptionEvent if the file is not found or if there was an error during
     *   saving or data conversion.
     */
    public void saveDataToFile(File file, ReadOnlyAddressBook data){
        try {
            FileUtil.createIfMissing(file);
            XmlFileStorage.saveDataToFile(file, new StorageAddressBook(data));
        } catch (IOException | DataConversionException e) {
            raise(new FileSavingExceptionEvent(e, file));
        }
    }

    /**
     * Loads the data from the local data file (based on user preferences).
     */
    public void start() {
        loadDataFromFile(userPrefs.getSaveLocation());
    }
}
