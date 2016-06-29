package address.model;

import address.util.AppLogger;
import address.util.LoggerManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;

/**
 * Represents User's preferences.
 */
public class UserPrefs {
    private static final String DEFAULT_TEMP_FILE_PATH = ".$TEMP_ADDRESS_BOOK";

    /**
     * Full path (including file name) of the data file to be used for local storage
     */
    private volatile StringProperty saveLocation;

    public UserPrefs() {
        saveLocation = new SimpleStringProperty("");
    }

    public synchronized void setSaveLocation(String saveLocation) {
        this.saveLocation.set(saveLocation);
    }

    /**
     * @return the current save file location or the default temp file location if there is no recorded preference.
     */
    public synchronized File getSaveLocation() {
        return saveLocation == null ? new File(DEFAULT_TEMP_FILE_PATH) : new File(saveLocation.get());
    }

    public StringProperty saveLocationProperty() {
        return saveLocation;
    }

    public boolean isSaveLocationSet() {
        return getSaveLocation() != null;
    }
}
