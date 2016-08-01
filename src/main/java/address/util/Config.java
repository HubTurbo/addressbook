package address.util;

import hubturbo.embeddedbrowser.BrowserType;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.util.HashMap;

/**
 * Config values used by the app
 */
public class Config {
    // Default values
    private static final long DEFAULT_UPDATE_INTERVAL = 10000;
    private static final Level DEFAULT_LOGGING_LEVEL = Level.INFO;
    private static final HashMap<String, Level> DEFAULT_SPECIAL_LOG_LEVELS = new HashMap<>();
    private static final int DEFAULT_BROWSER_NO_OF_PAGES = 3;
    private static final String DEFAULT_LOCAL_DATA_FILE_PATH = "data/addressbook.xml";
    private static final String DEFAULT_CLOUD_DATA_FILE_PATH = null; // For use in CloudManipulator for manual testing
    private static final String DEFAULT_ADDRESS_BOOK_NAME = "MyAddressBook";

    // Config values
    private String appTitle = "Address App";
    // Customizable through config file
    private long updateInterval = DEFAULT_UPDATE_INTERVAL;
    private Level currentLogLevel = DEFAULT_LOGGING_LEVEL;
    private HashMap<String, Level> specialLogLevels = DEFAULT_SPECIAL_LOG_LEVELS;
    private File prefsFileLocation = new File("preferences.json"); //Default user preferences file
    private int browserNoOfPages = DEFAULT_BROWSER_NO_OF_PAGES;
    private BrowserType browserType = BrowserType.LIMITED_FEATURE_BROWSER;
    private String localDataFilePath = DEFAULT_LOCAL_DATA_FILE_PATH;
    private String cloudDataFilePath = DEFAULT_CLOUD_DATA_FILE_PATH;
    private String addressBookName = DEFAULT_ADDRESS_BOOK_NAME;


    public Config() {
    }

    public String getAppTitle() {
        return appTitle;
    }

    public void setAppTitle(String appTitle) {
        this.appTitle = appTitle;
    }

    public long getUpdateInterval() {
        return updateInterval;
    }

    public void setUpdateInterval(long updateInterval) {
        this.updateInterval = updateInterval;
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

    public void setPrefsFileLocation(File prefsFileLocation) {
        this.prefsFileLocation = prefsFileLocation;
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

    public String getLocalDataFilePath() {
        return localDataFilePath;
    }

    public void setLocalDataFilePath(String localDataFilePath) {
        this.localDataFilePath = localDataFilePath;
    }

    public String getCloudDataFilePath() {
        return cloudDataFilePath;
    }

    public void setCloudDataFilePath(String cloudDataFilePath) {
        this.cloudDataFilePath = cloudDataFilePath;
    }

    public String getAddressBookName() {
        return addressBookName;
    }

    public void setAddressBookName(String addressBookName) {
        this.addressBookName = addressBookName;
    }


}
