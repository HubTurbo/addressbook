package address.storage;

import address.events.*;
import address.exceptions.DataConversionException;
import address.main.ComponentManager;
import address.model.ModelManager;
import address.model.UserPrefs;
import address.model.datatypes.ReadOnlyAddressBook;
import address.util.*;
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
    private static final String CONFIG_FILE = "config.json";

    public static final File DEFAULT_USER_PREF_FILE = new File("preferences.json");

    private ModelManager modelManager;
    private UserPrefs prefs;

    public StorageManager(ModelManager modelManager, UserPrefs prefs) {
        super();
        this.modelManager = modelManager;
        this.prefs = prefs;
    }

    public static Config getConfig() {
        File configFile = new File(CONFIG_FILE);

        Config config;
        if (configFile.exists()) {
            logger.info("Config file {} found, attempting to read.", configFile);
            config = readFromConfigFile(configFile);
        } else {
            logger.info("Config file {} not found, using default config.", configFile);
            config = new Config();
        }
        // Recreate the file so that any missing fields will be restored
        recreateFile(configFile, config);
        return config;
    }

    private static void recreateFile(File configFile, Config config) {
        if (!deleteConfigFileIfExists(configFile)) return;
        createAndWriteToConfigFile(configFile, config);
    }

    private static void createAndWriteToConfigFile(File configFile, Config config) {
        try {
            FileUtil.writeToFile(configFile, JsonUtil.toJsonString(config));
        } catch (IOException e) {
            logger.warn("Error writing to config file {}.", configFile);
        }
    }

    /**
     * Attempts to delete configFile if it exists
     *
     * @param configFile
     * @return false if exception is thrown
     */
    private static boolean deleteConfigFileIfExists(File configFile) {
        if (!FileUtil.isFileExists(configFile)) return true;

        try {
            FileUtil.deleteFile(configFile);
            return true;
        } catch (IOException e) {
            logger.warn("Error removing previous config file {}.", configFile);
            return false;
        }
    }

    /**
     * Attempts to read config values from the given file
     *
     * @param configFile
     * @return default config object if reading fails
     */
    private static Config readFromConfigFile(File configFile) {
        try {
            return JsonUtil.fromJsonString(FileUtil.readFromFile(configFile), Config.class);
        } catch (IOException e) {
            logger.warn("Error reading from config file {}: {}", configFile, e);
            return new Config();
        }
    }

    /**
     *  Raises a {@link address.events.FileOpeningExceptionEvent} if there was any problem in reading data from the file
     *  or if the file is not in the correct format.
     */
    @Subscribe
    public void handleLoadDataRequestEvent(LoadDataRequestEvent ldre) {
        File dataFile = ldre.file;
        logger.info("Handling load data request received: {}", dataFile);
        loadDataFromFile(dataFile);
    }

    /**
     * Raises FileSavingExceptionEvent (similar to {@link #saveDataToFile(File, ReadOnlyAddressBook)})
     */
    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent lmce) {
        logger.info("Local data changed, saving to primary data file");
        saveDataToFile(prefs.getSaveLocation(), lmce.data);
    }

    /**
     * Raises FileSavingExceptionEvent (similar to {@link #saveDataToFile(File, ReadOnlyAddressBook)})
     */
    @Subscribe
    public void handleSaveDataRequestEvent(SaveDataRequestEvent sdre) {
        logger.info("Save data request received: {}", sdre.data);
        saveDataToFile(sdre.file, sdre.data);
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
     * Raises FileSavingExceptionEvent
     */
    @Subscribe
    public void handleSavePrefsRequestEvent(SavePrefsRequestEvent spre) {
        logger.info("Save prefs request received: {}", spre.prefs);
        savePrefsToFile(spre.file, spre.prefs);
    }

    /**
     * Raises FileSavingExceptionEvent if there was an error during saving or data conversion.
     */
    public void savePrefsToFile(File file, UserPrefs prefs) {
        try {
            FileUtil.writeToFile(file, JsonUtil.toJsonString(prefs));
        } catch (IOException e) {
            raise(new FileSavingExceptionEvent(e, file));
        }
    }

    public static UserPrefs loadPrefsFromFile(File prefsFile) {
        UserPrefs prefs = new UserPrefs();

        if (!FileUtil.isFileExists(prefsFile)) {
            return prefs;
        }

        try {
            logger.debug("Attempting to load prefs from file: " + prefsFile);
            prefs = JsonUtil.fromJsonString(FileUtil.readFromFile(prefsFile), UserPrefs.class);
        } catch (IOException e) {
            logger.debug("Error loading prefs from file: {}", e);
        }

        return prefs;
    }

    /**
     * Loads the data from the local data file (based on user preferences).
     */
    public void start() {
        logger.info("Starting storage manager.");
        loadDataFromFile(prefs.getSaveLocation());
        raise(new SaveLocationChangedEvent(prefs.getSaveLocation()));
    }

    protected void loadDataFromFile(File dataFile) {
        try {
            logger.debug("Attempting to load data from file: {}", dataFile);
            modelManager.updateUsingExternalData(XmlFileStorage.loadDataFromSaveFile(dataFile));
        } catch (FileNotFoundException | DataConversionException e) {
            logger.debug("Error loading data from file: {}", e);
            raise(new FileOpeningExceptionEvent(e, dataFile));
        }
    }
}
