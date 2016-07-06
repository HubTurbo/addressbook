package address.model;

import address.util.ScreenUtil;
import javafx.util.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

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

    public ImmutablePair<Double, Double> screenSize;

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

    public ImmutablePair<Double, Double> getScreenSize() {
        return screenSize == null ? ScreenUtil.getRecommendedScreenSize() : screenSize;
    }

    public void setScreenSize(ImmutablePair<Double, Double> screenSize) {
        this.screenSize = screenSize;
    }
}
