package address.util;

import org.apache.logging.log4j.Level;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    private static final String MISSING_FIELD = "Missing field from {}: {}";

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
     * Creates a config object if needed
     *
     * Updates its fields based on values read from the config file if it exists
     * Else creates a new config file and/or uses default values
     *
     * @return
     */
    public static Config getConfig() {
        if (config == null) {
            config = new Config();
            if (config.hasExistingConfigFile()) {
                config.readFromConfigFile();
            } else {
                config.initializeConfigFile();
            }
        }
        return config;
    }

    private void initializeConfigFile() {
        File configFile = new File(CONFIG_FILE);
        try {
            logger.info("Creating config file.");
            createAndWriteToConfigFile(configFile);
        } catch (IOException e) {
            logger.warn("Error initializing config file.");
        }
    }

    private boolean hasExistingConfigFile() {
        File configFile = new File(CONFIG_FILE);
        if (!configFile.exists()) {
            logger.info("Config file not found.");
            return false;
        }
        return true;
    }

    /**
     * Reads from the config file
     */
    private void readFromConfigFile() {
        File configFile = new File(CONFIG_FILE);
        try {
            readAndSetConfigFileValues(new Ini(configFile));
        } catch (IOException e) {
            logger.warn("Error reading from config file.");
        }
    }

    private void readAndSetConfigFileValues(Ini iniFile) throws IOException {
        setMainSectionValues(iniFile.get(MAIN_SECTION));
        setLoggingSectionValues(iniFile.get(LOGGING_SECTION));
        setCloudSectionValues(iniFile.get(CLOUD_SECTION));
    }

    /**
     * Sets the config public variables according to the values read from the given section
     * Empty fields in specialLogLevels are treated as blank
     *
     * @param loggingSection
     * @throws IOException
     */
    private void setLoggingSectionValues(Profile.Section loggingSection) throws IOException {
        try {
            currentLogLevel = getLoggingLevel(getFieldValue(loggingSection, LOGGING_LEVEL));
        } catch (NoSuchFieldException e) {
            logger.warn(MISSING_FIELD, LOGGING_SECTION, LOGGING_LEVEL);
        }

        specialLogLevels = new HashMap<>();
        for (Level level : Level.values()) {
            try {
                getFieldList(loggingSection, level.toString()).stream()
                        .filter(classString -> !classString.equals(EMPTY_VALUE))
                        .forEach(classString -> specialLogLevels.put(classString, level));
            } catch (NoSuchFieldException e) {
                logger.warn(MISSING_FIELD, LOGGING_SECTION, level.toString());
            }
        }
    }

    private void setMainSectionValues(Profile.Section mainSection) throws IOException {
        try {
            updateInterval = Long.parseLong(getFieldValue(mainSection, UPDATE_INTERVAL));
        } catch (NoSuchFieldException e) {
            logger.warn(MISSING_FIELD, MAIN_SECTION, UPDATE_INTERVAL);
        }
    }

    private void setCloudSectionValues(Profile.Section cloudSection) {
        try {
            simulateUnreliableNetwork = Boolean.parseBoolean(getFieldValue(cloudSection, UNRELIABLE_NETWORK));
        } catch (NoSuchFieldException e) {
            logger.warn(MISSING_FIELD, CLOUD_SECTION, UNRELIABLE_NETWORK);
        }
    }

    private String getFieldValue(Profile.Section mainSection, String fieldName) throws NoSuchFieldException {
        String updateInterval = mainSection.get(fieldName);
        if (updateInterval == null) throw new NoSuchFieldException(fieldName);
        return updateInterval;
    }

    private List<String> getFieldList(Profile.Section mainSection, String fieldName) throws NoSuchFieldException {
        List<String> updateInterval = mainSection.getAll(fieldName);
        if (updateInterval == null) throw new NoSuchFieldException(fieldName);
        return updateInterval;
    }

    /**
     * Creates a new config file and writes the current config values of this object
     * into the config file
     *
     * @param configFile
     * @throws IOException
     */
    private void createAndWriteToConfigFile(File configFile) throws IOException {
        if (!configFile.createNewFile()) return;
        Ini ini = new Ini(configFile);

        putMainSection(ini);
        putLoggingSection(ini);
        putCloudSection(ini);

        ini.store();
    }

    private void putCloudSection(Ini ini) {
        ini.put(CLOUD_SECTION, UNRELIABLE_NETWORK, simulateUnreliableNetwork);
    }

    private void putLoggingSection(Ini ini) {
        ini.put(LOGGING_SECTION, LOGGING_LEVEL, currentLogLevel);
        for (Level level : Level.values()) {
            List<String> specialClassesForCurLevel = getSpecialLogClassesForLevel(level, specialLogLevels);
            if (specialClassesForCurLevel.size() > 0) {
                ini.get(LOGGING_SECTION).putAll(level.toString(), specialClassesForCurLevel);
            } else {
                // blank field if empty, instead of omitting field entirely
                ini.get(LOGGING_SECTION).put(level.toString(), EMPTY_VALUE);
            }
        }
    }

    private List<String> getSpecialLogClassesForLevel(Level level, HashMap<String, Level> specialLogLevels) {
        Set<String> allSpecialClasses = specialLogLevels.keySet();
        return allSpecialClasses.stream()
                .filter(specialClass -> level.equals(specialLogLevels.get(specialClass)))
                .collect(Collectors.toList());
    }

    private void putMainSection(Ini ini) {
        ini.put(MAIN_SECTION, UPDATE_INTERVAL, updateInterval);
    }

    /**
     * Gets the logging level that matches loggingLevelString
     *
     * Returns the default logging level if there are no matches
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
