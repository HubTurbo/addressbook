package address.util;

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

    // Config values
    public String appTitle = "Address App";
    // Customizable through config file
    public long updateInterval = DEFAULT_UPDATE_INTERVAL;
    public boolean simulateUnreliableNetwork = DEFAULT_NETWORK_UNRELIABLE_MODE;
    public Level currentLogLevel = DEFAULT_LOGGING_LEVEL;
    public HashMap<String, Level> specialLogLevels = DEFAULT_SPECIAL_LOG_LEVELS;
    private File prefsFileLocation = new File("preferences.json"); //Default user preferences file


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
}
