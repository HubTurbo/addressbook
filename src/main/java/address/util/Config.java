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

    private static final long DEFAULT_UPDATE_INTERVAL = 10000;
    private static final boolean DEFAULT_NETWORK_UNRELIABLE_MODE = false;
    private static final Level DEFAULT_LOGGING_LEVEL = Level.INFO;


    public String appTitle = "Address App";
    public long updateInterval = DEFAULT_UPDATE_INTERVAL;
    public boolean simulateUnreliableNetwork = DEFAULT_NETWORK_UNRELIABLE_MODE;
    public Level currentLogLevel = DEFAULT_LOGGING_LEVEL;
    public HashMap<String, Level> specialLogLevel;


    private static Config config;

    public static void setConfig(Config configToSet) {
        config = configToSet;
    }

    public static Config getConfig() {
        if (config == null) {
            config = new Config();
        }
        return config;
    }

    public Config() {
        try {
            File configFile = new File("config.ini");
            if (configFile.exists()) {
                setConfigValues(new Ini(configFile));
            } else {
                createConfigWithDefaults(configFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setConfigValues(Ini iniFile) throws IOException {
        setMainSectionValues(iniFile.get("Main"));
        setLoggingSectionValues(iniFile.get("Logging"));
        setCloudSectionValues(iniFile.get("Cloud"));
    }

    private void setLoggingSectionValues(Profile.Section loggingSection) throws IOException {
        currentLogLevel = getLoggingLevel(loggingSection);
        specialLogLevel = getSpecialLoggingClasses(loggingSection);
    }

    private void setMainSectionValues(Profile.Section mainSection) throws IOException {
        updateInterval = getUpdateInterval(mainSection);
    }

    private void setCloudSectionValues(Profile.Section cloudSection) {
        simulateUnreliableNetwork = Boolean.parseBoolean(cloudSection.get("unreliable"));
    }

    private long getUpdateInterval(Profile.Section mainSection) {
        return Long.parseLong(mainSection.get("updateInterval"));
    }

    private Level getLoggingLevel(Profile.Section section) {
        String loggingLevelString = section.get("LoggingLevel");
        return determineLoggingLevel(loggingLevelString);
    }

    private void createConfigWithDefaults(File configFile) throws IOException {
        if (configFile.createNewFile()) {
            Wini wini = new Wini(configFile);

            // main
            wini.put("Main", "updateInterval", DEFAULT_UPDATE_INTERVAL);

            // logging
            wini.put("Logging", "LoggingLevel", DEFAULT_LOGGING_LEVEL.toString());
            Level[] allLoggingLevels = Level.values();
            for (Level level : allLoggingLevels) {
                wini.put("Logging", level.toString(), "");
            }

            // cloud
            wini.put("Cloud", "unreliable", DEFAULT_NETWORK_UNRELIABLE_MODE);

            wini.store();
        }
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
