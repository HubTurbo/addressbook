package address.model;

import address.util.ScreenSize;
import address.util.ScreenUtil;

import java.io.File;

/**
 * Represents User's preferences.
 */
public class UserPrefs {

    private static final String DEFAULT_TEMP_FILE_PATH = ".$TEMP_ADDRESS_BOOK";

    /**
     * Full path (including file name) of the data file to be used for local storage
     */
    private volatile String saveLocation;

    public ScreenSize screenSize;

    public synchronized void setSaveLocation(String saveLocation) {
        this.saveLocation = saveLocation;
    }

    /**
     * @return the current save file location or the default temp file location if there is no recorded preference.
     */
    public synchronized File getSaveLocation() {
        return saveLocation == null ? new File(DEFAULT_TEMP_FILE_PATH) : new File(saveLocation);
    }

    public String getSaveFileName() {
        return getSaveLocation().getName();
    }

    public String getSaveLocationString() {
        return saveLocation;
    }

    public boolean isSaveLocationSet() {
        return getSaveLocation() != null;
    }

    public ScreenSize getScreenSize() {
        return screenSize == null ? ScreenUtil.getRecommendedScreenSize() : screenSize;
    }

    public void setScreenSize(ScreenSize screenSize) {
        this.screenSize = screenSize;
    }
}
