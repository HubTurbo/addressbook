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
    public String appTitle = "Address App";
    public long updateInterval = 10000;
    public boolean simulateUnreliableNetwork = false;
    public Level defaultLogLevel = Level.INFO;

    public Config() {
        try {
            File configFile = new File("config.ini");
            if (configFile.exists()) {
                Profile.Section section = new Ini(configFile).get("Logging");
                LoggerManager.currentLogLevel = getLoggingLevelFromSection(section);
            } else {
                createConfigWithDefaults(configFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Level getLoggingLevelFromSection(Profile.Section section) {
        String loggingLevelString = section.get("LoggingLevel");
        return determineLoggingLevel(loggingLevelString);
    }

    private void createConfigWithDefaults(File configFile) throws IOException {
        if (configFile.createNewFile()) {
            Wini wini = new Wini(configFile);
            wini.put("Logging", "LoggingLevel", defaultLogLevel.toString());
            Level[] allLoggingLevels = Level.values();
            for (Level level : allLoggingLevels) {
                wini.put("Logging", level.toString(), "");
            }

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
        return defaultLogLevel;
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
