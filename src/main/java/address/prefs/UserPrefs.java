package address.prefs;

import address.util.AppLogger;
import address.util.FileUtil;
import address.util.JsonUtil;
import address.util.LoggerManager;

import java.io.File;
import java.io.IOException;

/**
 * Represents User's preferences.
 */
public class UserPrefs {
    private static final AppLogger logger = LoggerManager.getLogger(UserPrefs.class);

    private static final File DEFAULT_USER_PREF_FILE = new File("pref");

    private static final String DEFAULT_TEMP_FILE_PATH = ".$TEMP_ADDRESS_BOOK";

    /**
     * Full path (including file name) of the data file to be used for local storage
     */
    private String saveLocation;

    public void setSaveLocation(String saveLocation) {
        synchronized(this) {
            this.saveLocation = saveLocation;
        }

        writePrefToFile();
    }

    /**
     * @return the current save file location or the default temp file location if there is no recorded preference.
     */
    public synchronized File getSaveLocation() {
        return saveLocation == null ? new File(DEFAULT_TEMP_FILE_PATH) : new File(saveLocation);
    }

    private void writePrefToFile() {
        writePrefToFile(DEFAULT_USER_PREF_FILE, this);
    }

    private synchronized void writePrefToFile(File file, UserPrefs userPrefs) {
        try {
            FileUtil.writeToFile(file, JsonUtil.toJsonString(userPrefs));
        } catch (IOException e) {
            logger.info("Failed to write to preference file", e);
        }
    }

    public static UserPrefs readPrefFromFile() throws IOException {
        return readPrefFromFile(DEFAULT_USER_PREF_FILE);
    }

    private synchronized static UserPrefs readPrefFromFile(File file) throws IOException {
        if (!FileUtil.isFileExists(file)) {
            logger.info("The specified user pref file does not exist");
            return new UserPrefs();
        }

        return JsonUtil.fromJsonString(FileUtil.readFromFile(file), UserPrefs.class);
    }
}
