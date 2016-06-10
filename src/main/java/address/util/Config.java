package address.util;

import org.apache.logging.log4j.Level;
import org.ini4j.Ini;
import org.ini4j.Profile;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;

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
                Profile.Section section = new Ini(configFile).get("main");
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
            wini.put("main", "LoggingLevel", defaultLogLevel.toString());
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


}
