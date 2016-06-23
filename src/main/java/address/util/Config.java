package address.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
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

    private static final AppLogger logger = LoggerManager.getLogger(Config.class);

    private static final String CONFIG_FILE = "config.ini";
    private static final String MISSING_FIELD = "Missing field from {}: {}";
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

    // Config values
    public String appTitle = "Address App";
    // Customizable through config file
    public long updateInterval = DEFAULT_UPDATE_INTERVAL;
    public boolean simulateUnreliableNetwork = DEFAULT_NETWORK_UNRELIABLE_MODE;
    public Level currentLogLevel = DEFAULT_LOGGING_LEVEL;
    public HashMap<String, Level> specialLogLevels = DEFAULT_SPECIAL_LOG_LEVELS;

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

    public void readFromConfigFile() {
        if (!hasExistingConfigFile() || !setConfigFileValues()) {
            initializeConfigFile();
        }
    }

    private void initializeConfigFile() {
        File configFile = new File(CONFIG_FILE);
        try {
            logger.info("Initializing config file.");
            recreateConfigFile(configFile);
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
     * Reads from the config file, and updates this object's values
     * Missing fields are skipped from reading
     *
     * @return false if there are errors or missing fields
     */
    private boolean setConfigFileValues() {
        try {
            File configFile = new File(CONFIG_FILE);
            Ini iniFile = new Ini(configFile);
            boolean hasAllFields = updateValuesReadFromIniFile(iniFile);
            if (hasAllFields) {
                logger.info("Config values successfully read: '{}'", CONFIG_FILE);
            } else {
                logger.warn("Config fields are missing: '{}'", CONFIG_FILE);
            }
            return hasAllFields;
        } catch (IOException e) {
            logger.warn("Error reading from config file.");
            return false;
        }
    }

    /**
     * Updates this instance's values using values found in iniFile
     *
     * Missing fields in iniFile will not be updated
     *
     * @param iniFile
     * @return false if there are missing fields
     * @throws IOException
     */
    private boolean updateValuesReadFromIniFile(Ini iniFile) throws IOException {
        boolean hasAllFields = true;

        if (!updateMainSectionValues(iniFile.get(MAIN_SECTION))) hasAllFields = false;
        if (!updateLoggingSectionValues(iniFile.get(LOGGING_SECTION))) hasAllFields = false;
        if (!updateCloudSectionValues(iniFile.get(CLOUD_SECTION))) hasAllFields = false;

        return hasAllFields;
    }

    /**
     * Sets the config special logging classes' variables according to the values read from the given logging section
     * Empty fields in specialLogLevels are assumed to be blank rather than omitting the field entirely
     *
     * @param loggingSection
     */
    private boolean updateLoggingSectionValues(Profile.Section loggingSection) {
        boolean hasAllFields = true;
        try {
            currentLogLevel = getLoggingLevel(getFieldValue(loggingSection, LOGGING_LEVEL));
        } catch (NoSuchFieldException e) {
            logger.warn(MISSING_FIELD, LOGGING_SECTION, LOGGING_LEVEL);
            hasAllFields = false;
        }

        specialLogLevels = new HashMap<>();
        for (Level level : Level.values()) {
            try {
                getFieldList(loggingSection, level.toString()).stream()
                        .filter(classString -> !classString.equals(EMPTY_VALUE))
                        .forEach(classString -> specialLogLevels.put(classString, level));
            } catch (NoSuchFieldException e) {
                logger.warn(MISSING_FIELD, LOGGING_SECTION, level.toString());
                hasAllFields = false;
            }
        }

        return hasAllFields;
    }

    private boolean updateMainSectionValues(Profile.Section mainSection) {
        boolean hasAllFields = true;
        try {
            updateInterval = Long.parseLong(getFieldValue(mainSection, UPDATE_INTERVAL));
        } catch (NoSuchFieldException e) {
            logger.warn(MISSING_FIELD, MAIN_SECTION, UPDATE_INTERVAL);
            hasAllFields = false;
        }
        return hasAllFields;
    }

    private boolean updateCloudSectionValues(Profile.Section cloudSection) {
        boolean hasAllFields = true;
        try {
            simulateUnreliableNetwork = Boolean.parseBoolean(getFieldValue(cloudSection, UNRELIABLE_NETWORK));
        } catch (NoSuchFieldException e) {
            logger.warn(MISSING_FIELD, CLOUD_SECTION, UNRELIABLE_NETWORK);
            hasAllFields = false;
        }
        return hasAllFields;
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
     * Deletes any existing config file, then creates a new config file and
     * writes the current config values of this object into the config file
     *
     * @param configFile file to check for and/or to create
     * @throws IOException
     */
    private void recreateConfigFile(File configFile) throws IOException {
        if (configFile.exists() && !configFile.delete()) throw new IOException("Error removing existing config file.");
        if (!configFile.createNewFile()) throw new IOException("Error creating new config file.");
        Ini ini = new Ini(configFile);

        fillMainSection(ini);
        fillLoggingSection(ini);
        fillCloudSection(ini);

        ini.store();
    }

    private void fillCloudSection(Ini ini) {
        ini.put(CLOUD_SECTION, UNRELIABLE_NETWORK, simulateUnreliableNetwork);
    }

    private void fillLoggingSection(Ini ini) {
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

    private void fillMainSection(Ini ini) {
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
        logger.warn("Invalid logging level: {}. Using default: {}", loggingLevelString, DEFAULT_LOGGING_LEVEL);
        return DEFAULT_LOGGING_LEVEL;
    }
}
