package address.util;

import hubturbo.embeddedbrowser.BrowserType;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.HashMap;

/**
 * Config values used by the app
 */
public class Config {
    private static final String CONFIG_FILE = "config.ini";

    // Default values
    private static final long DEFAULT_UPDATE_INTERVAL = 10000;
    private static final Level DEFAULT_LOGGING_LEVEL = Level.INFO;
    private static final boolean DEFAULT_NETWORK_UNRELIABLE_MODE = false;
    private static final HashMap<String, Level> DEFAULT_SPECIAL_LOG_LEVELS = new HashMap<>();
    private static final int DEFAULT_BROWSER_NO_OF_PAGES = 3;
    private static final BrowserType DEFAULT_BROWSER_TYPE = BrowserType.FULL_FEATURE_BROWSER;
    private static final boolean DEFAULT_IS_CLOUD_MANIPULABLE = false;

    // Config values
    public String appTitle = "Address App";
    // Customizable through config file
    public long updateInterval = DEFAULT_UPDATE_INTERVAL;
    public boolean simulateUnreliableNetwork = DEFAULT_NETWORK_UNRELIABLE_MODE;
    public Level currentLogLevel = DEFAULT_LOGGING_LEVEL;
    public HashMap<String, Level> specialLogLevels = DEFAULT_SPECIAL_LOG_LEVELS;
    private File prefsFileLocation = new File("preferences.json"); //Default user preferences file
    public int browserNoOfPages = DEFAULT_BROWSER_NO_OF_PAGES;
    public BrowserType browserType = DEFAULT_BROWSER_TYPE;
    public boolean isCloudManipulable = DEFAULT_IS_CLOUD_MANIPULABLE;


    public Config() {
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
    }

    public boolean isSimulateUnreliableNetwork() {
        return simulateUnreliableNetwork;
    }

    public void setSimulateUnreliableNetwork(boolean simulateUnreliableNetwork) {
        this.simulateUnreliableNetwork = simulateUnreliableNetwork;
    }

    public Level getCurrentLogLevel() {
        return currentLogLevel;
    }

    public void setCurrentLogLevel(Level currentLogLevel) {
        this.currentLogLevel = currentLogLevel;
    }

    public HashMap<String, Level> getSpecialLogLevels() {
        return specialLogLevels;
    }

    public void setSpecialLogLevels(HashMap<String, Level> specialLogLevels) {
        this.specialLogLevels = specialLogLevels;
    }

    public File getPrefsFileLocation() {
        return prefsFileLocation;
    }

    public int getBrowserNoOfPages() {
        return browserNoOfPages;
    }

    public void setBrowserNoOfPages(int browserNoOfPages) {
        this.browserNoOfPages = browserNoOfPages;
    }

    public BrowserType getBrowserType() {
        return browserType;
    }

    public void setBrowserType(BrowserType browserType) {
        this.browserType = browserType;
    }
}
