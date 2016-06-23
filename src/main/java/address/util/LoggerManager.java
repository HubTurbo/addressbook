package address.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.HashMap;

public class LoggerManager {
    public static Level currentLogLevel = Level.INFO;
    public static HashMap<String, Level> specialLogLevel = new HashMap<>();

    public static void updateWithConfig(Config config) {
        currentLogLevel = config.currentLogLevel;
        specialLogLevel = config.specialLogLevels;
    }

    public static AppLogger getLogger(String className, Level loggingLevel) {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        AbstractConfiguration config = (AbstractConfiguration) loggerContext.getConfiguration();
        setLoggingLevel(config, className, loggingLevel);
        loggerContext.updateLoggers(config);
        return new AppLogger(LogManager.getLogger(className));
    }

    private static Level determineLoggingLevelToSet(String className) {
        if (specialLogLevel != null && specialLogLevel.containsKey(className)) {
            return specialLogLevel.get(className);
        }
        return currentLogLevel;
    }

    private static void setLoggingLevel(AbstractConfiguration config, String className, Level loggingLevel) {
        if (config.getLogger(className) != null) {
            config.getLoggerConfig(className).setLevel(loggingLevel);
            return;
        }

        config.addLogger(className, new LoggerConfig(className, loggingLevel, true));
    }

    public static AppLogger getLogger(String className) {
        Level loggingLevelToSet = determineLoggingLevelToSet(className);

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        AbstractConfiguration config = (AbstractConfiguration) loggerContext.getConfiguration();
        setLoggingLevel(config, className, loggingLevelToSet);
        loggerContext.updateLoggers();
        return new AppLogger(LogManager.getLogger(className));
    }

    public static <T> AppLogger getLogger(Class<T> clazz) {
        if (clazz == null) return new AppLogger(LogManager.getRootLogger());
        return getLogger(clazz.getSimpleName());
    }
}
