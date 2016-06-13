package address.util;

import org.apache.logging.log4j.Level;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Config values used by the app
 */
public class Config {

    private static final String CONFIG_FILE = "config.ini";
    private static final String EMPTY_VALUE = "";

    // Config variables grouped by sections
    private static final String MAIN_SECTION = "Main";
    private static final String UPDATE_INTERVAL = "updateInterval";

    private static final String LOGGING_SECTION = "Logging";
    private static final String LOGGING_LEVEL = "loggingLevel";

    private static final String CLOUD_SECTION = "Cloud";
    private static final String UNRELIABLE_NETWORK = "unreliableNetwork";

    // Default values
    private static final long DEFAULT_UPDATE_INTERVAL = 10000;
    private static final Level DEFAULT_LOGGING_LEVEL = Level.INFO;
    private static final boolean DEFAULT_NETWORK_UNRELIABLE_MODE = false;
    private static final HashMap<String, Level> DEFAULT_SPECIAL_LOG_LEVELS = new HashMap<>();
    public static final String MISSING_FIELDS = "Missing field from {}: {}";

    // Config values
    public String appTitle = "Address App";
    // Customizable through config file
    public long updateInterval = DEFAULT_UPDATE_INTERVAL;
    public boolean simulateUnreliableNetwork = DEFAULT_NETWORK_UNRELIABLE_MODE;
    public Level currentLogLevel = DEFAULT_LOGGING_LEVEL;
    public HashMap<String, Level> specialLogLevels = DEFAULT_SPECIAL_LOG_LEVELS;

    private static Config config;
    private AppLogger logger = LoggerManager.getLogger(Config.class);

    public static void setConfig(Config configToSet) {
        config = configToSet;
    }

    /**
     * Lazy initialization of global config object
     * <p>
     * Contains read values from the config file fields if they exist
     * Fields not found in the config file will be set to defaults
     *
     * @return
     */
    public static Config getConfig() {
        if (config == null) {
            config = new Config();
            config.readFromConfigFileIfExists();
        }
        return config;
    }

    /**
     * Reads from the config file if it exists
     *
     * Else creates a config file with default values
     */
    private void readFromConfigFileIfExists() {
        File configFile = new File(CONFIG_FILE);
        try {
            if (configFile.exists()) {
                readAndSetConfigFileValues(new Ini(configFile));
                return;
            }
            LoggerManager.getLogger(Config.class).info("Config file not found.");
        } catch (IOException e) {
            LoggerManager.getLogger(Config.class).warn("Error reading from config file.");
        }

        try {
            LoggerManager.getLogger(Config.class).info("Creating config file.");
            createConfigFileWithDefaults(configFile);
        } catch (IOException e) {
            LoggerManager.getLogger(Config.class).warn("Error initializing config file.");
        }
    }

    private void readAndSetConfigFileValues(Ini iniFile) throws IOException {
        setMainSectionValues(iniFile.get(MAIN_SECTION));
        setLoggingSectionValues(iniFile.get(LOGGING_SECTION));
        setCloudSectionValues(iniFile.get(CLOUD_SECTION));
    }

    private void setLoggingSectionValues(Profile.Section loggingSection) throws IOException {
        try {
            currentLogLevel = getLoggingLevel(getFieldValue(loggingSection, LOGGING_LEVEL));
        } catch (NoSuchFieldException e) {
            logger.warn(MISSING_FIELDS, LOGGING_SECTION, e.toString());
        }

        specialLogLevels = new HashMap<>();
        for (Level level : Level.values()) {
            try {
                getFieldArray(loggingSection, level.toString()).stream()
                        .forEach(classString -> specialLogLevels.put(classString, level));
            } catch (NoSuchFieldException e) {
                logger.warn(MISSING_FIELDS, LOGGING_SECTION, e.toString());
            }
        }
    }

    private void setMainSectionValues(Profile.Section mainSection) throws IOException {
        try {
            updateInterval = Long.parseLong(getFieldValue(mainSection, UPDATE_INTERVAL));
        } catch (NoSuchFieldException e) {
            logger.warn(MISSING_FIELDS, MAIN_SECTION, e.toString());
        }
    }

    private void setCloudSectionValues(Profile.Section cloudSection) {
        try {
            simulateUnreliableNetwork = Boolean.parseBoolean(getFieldValue(cloudSection, UNRELIABLE_NETWORK));
        } catch (NoSuchFieldException e) {
            logger.warn(MISSING_FIELDS, CLOUD_SECTION, e.toString());
        }
    }

    private String getFieldValue(Profile.Section mainSection, String fieldName) throws NoSuchFieldException {
        String updateInterval = mainSection.get(fieldName);
        if (updateInterval == null) throw new NoSuchFieldException(fieldName);
        return updateInterval;
    }

    private List<String> getFieldArray(Profile.Section mainSection, String fieldName) throws NoSuchFieldException {
        List<String> updateInterval = mainSection.getAll(fieldName);
        if (updateInterval == null) throw new NoSuchFieldException(fieldName);
        return updateInterval;
    }

    private void createConfigFileWithDefaults(File configFile) throws IOException {
        if (!configFile.createNewFile()) return;
        Wini wini = new Wini(configFile);

        putMainSectionDefaults(wini);
        putLoggingSectionDefaults(wini);
        putCloudSectionDefaults(wini);

        wini.store();
    }

    private void putCloudSectionDefaults(Wini wini) {
        wini.put(CLOUD_SECTION, UNRELIABLE_NETWORK, DEFAULT_NETWORK_UNRELIABLE_MODE);
    }

    private void putLoggingSectionDefaults(Wini wini) {
        wini.put(LOGGING_SECTION, LOGGING_LEVEL, DEFAULT_LOGGING_LEVEL);
        for (Level level : Level.values()) {
            wini.put(LOGGING_SECTION, level.toString(), EMPTY_VALUE);
        }
    }

    private void putMainSectionDefaults(Wini wini) {
        wini.put(MAIN_SECTION, UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL);
    }

    /**
     * Gets the logging level that matches loggingLevelString
     *
     * @param loggingLevelString
     * @return
     */
    private Level getLoggingLevel(String loggingLevelString) {
        for (Level level : Level.values()) {
            if (level.toString().equals(loggingLevelString)) {
                return level;
            }
        }
        logger.warn("Invalid logging level. Using default: " + DEFAULT_LOGGING_LEVEL);
        return DEFAULT_LOGGING_LEVEL;
    }
}
