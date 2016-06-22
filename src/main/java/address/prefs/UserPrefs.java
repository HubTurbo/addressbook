package address.prefs;

import java.io.File;

/**
 * Represents User's preferences.
 */
public class UserPrefs {
    public static final String DEFAULT_TEMP_FILE_PATH = ".$TEMP_ADDRESS_BOOK";


    /**
     * Full path (including file name) of the data file to be used for local storage
     */

    protected String saveLocation;

    /**
     * @return the current save file location or the default temp file location if there is no recorded preference.
     */
    public File getSaveLocation() {
        return saveLocation == null ? new File(DEFAULT_TEMP_FILE_PATH) : new File(saveLocation);
    }
}
