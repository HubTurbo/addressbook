package address.storage;

import address.events.*;
import address.exceptions.DataConversionException;
import address.main.ComponentManager;
import address.model.UserPrefs;
import address.model.datatypes.ReadOnlyAddressBook;
import address.util.*;
import com.google.common.eventbus.Subscribe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Manages storage of addressbook data in local disk.
 * Handles storage related events.
 */
public class StorageManager extends ComponentManager {
    private static final AppLogger logger = LoggerManager.getLogger(StorageManager.class);
    private static final String CONFIG_FILE = "config.json";
    private Config config;

    private final Consumer<ReadOnlyAddressBook> loadedDataCallback;
    private UserPrefs prefs;

    public StorageManager(Consumer<ReadOnlyAddressBook> loadedDataCallback, Config config, UserPrefs prefs) {
        super();
        this.loadedDataCallback = loadedDataCallback;
        this.prefs = prefs;
        this.config = config;
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
            serializeObjectToJsonFile(configFile, config);
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
            return deserializeObjectFromJsonFile(configFile, Config.class);
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
     * saving or data conversion.
     */
    public void saveDataToFile(File file, ReadOnlyAddressBook data){
        try {
            saveAddressBook(file, data);
        } catch (IOException | DataConversionException e) {
            raise(new FileSavingExceptionEvent(e, file));
        }
    }

    /**
     * Saves the address book data in the file specified.
     */
    public static void saveAddressBook(File file, ReadOnlyAddressBook data) throws IOException, DataConversionException {
        FileUtil.createIfMissing(file);
        XmlFileStorage.saveDataToFile(file, new StorageAddressBook(data));
    }

    /**
     * Raises FileSavingExceptionEvent
     */
    @Subscribe
    public void handleSavePrefsRequestEvent(SavePrefsRequestEvent spre) {
        logger.info("Save prefs request received: {}", spre.prefs);
        savePrefsToFile(spre.prefs);
    }

    /**
     * Raises FileSavingExceptionEvent if there was an error during saving or data conversion.
     */
    public void savePrefsToFile(UserPrefs prefs) {
        try {
            serializeObjectToJsonFile(config.getPrefsFileLocation(), prefs);
        } catch (IOException e) {
            raise(new FileSavingExceptionEvent(e, config.getPrefsFileLocation()));
        }
    }

    public static UserPrefs getUserPrefs(File prefsFile) {
        UserPrefs prefs = new UserPrefs();

        if (!FileUtil.isFileExists(prefsFile)) {
            return prefs;
        }

        try {
            logger.debug("Attempting to load prefs from file: {}", prefsFile);
            prefs = deserializeObjectFromJsonFile(prefsFile, UserPrefs.class);
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
    }

    protected void loadDataFromFile(File dataFile) {
        try {
            logger.debug("Attempting to load data from file: {}", dataFile);
            loadedDataCallback.accept(getData());
        } catch (FileNotFoundException | DataConversionException e) {
            logger.debug("Error loading data from file: {}", e);
            raise(new FileOpeningExceptionEvent(e, dataFile));
        }
    }

    public ReadOnlyAddressBook getData() throws FileNotFoundException, DataConversionException {
        logger.debug("Attempting to read data from file: {}", prefs.getSaveLocation());
        return XmlFileStorage.loadDataFromSaveFile(prefs.getSaveLocation());
    }

    public static <T> void serializeObjectToJsonFile(File jsonFile, T objectToSerialize) throws IOException {
        FileUtil.writeToFile(jsonFile, JsonUtil.toJsonString(objectToSerialize));
    }

    public static <T> T deserializeObjectFromJsonFile(File jsonFile, Class<T> classOfObjectToDeserialize)
            throws IOException {
        return JsonUtil.fromJsonString(FileUtil.readFromFile(jsonFile), classOfObjectToDeserialize);
    }
}
