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
    private static final HashMap<String, Level> DEFAULT_SPECIAL_LOG_LEVEL = new HashMap<>();

    // Config values
    public String appTitle = "Address App";
    // Customizable through config file
    public long updateInterval = DEFAULT_UPDATE_INTERVAL;
    public boolean simulateUnreliableNetwork = DEFAULT_NETWORK_UNRELIABLE_MODE;
    public Level currentLogLevel = DEFAULT_LOGGING_LEVEL;
    public HashMap<String, Level> specialLogLevel = DEFAULT_SPECIAL_LOG_LEVEL;

    private static Config config;

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
        currentLogLevel = getLoggingLevel(loggingSection);
        specialLogLevel = getSpecialLoggingClasses(loggingSection);
    }

    private void setMainSectionValues(Profile.Section mainSection) throws IOException {
        updateInterval = getUpdateInterval(mainSection);
    }

    private void setCloudSectionValues(Profile.Section cloudSection) {
        simulateUnreliableNetwork = Boolean.parseBoolean(cloudSection.get(UNRELIABLE_NETWORK));
    }

    private long getUpdateInterval(Profile.Section mainSection) {
        return Long.parseLong(mainSection.get(UPDATE_INTERVAL));
    }

    private Level getLoggingLevel(Profile.Section section) {
        String loggingLevelString = section.get(LOGGING_LEVEL);
        return determineLoggingLevel(loggingLevelString);
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
        wini.put(LOGGING_SECTION, LOGGING_LEVEL, DEFAULT_LOGGING_LEVEL.toString());
        for (Level level : Level.values()) {
            wini.put(LOGGING_SECTION, level.toString(), EMPTY_VALUE);
        }
    }

    private void putMainSectionDefaults(Wini wini) {
        wini.put(MAIN_SECTION, UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL);
    }

    private Level determineLoggingLevel(String loggingLevelString) {
        Level[] allLoggingLevels = Level.values();
        for (Level level : allLoggingLevels) {
            if (level.toString().equals(loggingLevelString)) {
                return level;
            }
        }
        return DEFAULT_LOGGING_LEVEL;
    }

    private HashMap<String, Level> getSpecialLoggingClasses(Profile.Section section) {
        HashMap<String, Level> specialLoggingClasses = new HashMap<>();
        Level[] allLoggingLevels = Level.values();
        for (Level level : allLoggingLevels) {
            addSpecialLoggingClasses(section, specialLoggingClasses, level);
        }
        return specialLoggingClasses;
    }

    private void addSpecialLoggingClasses(Profile.Section section, HashMap<String, Level> specialLoggingClasses, Level loggingLevel) {
        List<String> infoClasses = section.getAll(loggingLevel.toString());
        infoClasses.stream()
                .forEach(infoClass -> specialLoggingClasses.put(infoClass, loggingLevel));
    }
}
