package address.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.HashMap;

public class LoggerManager {
    private static final AppLogger logger = LoggerManager.getLogger(LoggerManager.class);
    public static Level currentLogLevel = Level.INFO;
    public static HashMap<String, Level> specialLogLevels = new HashMap<>();

    public static void init(Config config) {
        logger.info("currentLogLevel: {}", config.currentLogLevel);
        logger.info("specialLogLevels: {}", config.specialLogLevels);
        currentLogLevel = config.currentLogLevel;
        specialLogLevels = config.specialLogLevels;

        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        AbstractConfiguration absConfig = (AbstractConfiguration) loggerContext.getConfiguration();
        absConfig.getLoggers().forEach((loggerName, loggerConfig) -> {
            if (specialLogLevels.containsKey(loggerName)) {
                loggerConfig.setLevel(specialLogLevels.get(loggerName));
            } else {
                loggerConfig.setLevel(currentLogLevel);
            }
        });
    }

    public static AppLogger getLogger(String className, Level loggingLevel) {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        AbstractConfiguration config = (AbstractConfiguration) loggerContext.getConfiguration();
        setLoggingLevel(config, className, loggingLevel);
        loggerContext.updateLoggers(config);
        return new AppLogger(LogManager.getLogger(className));
    }

    private static Level determineLoggingLevelToSet(String className) {
        if (specialLogLevels != null && specialLogLevels.containsKey(className)) {
            return specialLogLevels.get(className);
        }
        return currentLogLevel;
    }

    private static void setLoggingLevel(AbstractConfiguration config, String className, Level loggingLevel) {
        if (config.getLogger(className) != null) {
            config.getLogger(className).setLevel(loggingLevel);
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
