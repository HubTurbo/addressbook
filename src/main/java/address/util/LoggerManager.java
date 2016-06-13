package address.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.HashMap;

public class LoggerManager {
    public static Level currentLogLevel  = Config.getConfig().currentLogLevel;
    public static HashMap<String, Level> specialLogLevel = Config.getConfig().specialLogLevel;

    public static AppLogger getLogger(String className, Level loggingLevel) {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration config = loggerContext.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(className);
        loggerConfig.setLevel(loggingLevel);
        loggerContext.updateLoggers(config);
        return new AppLogger(LogManager.getLogger(className));
    }

    public static AppLogger getLogger(String className) {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration config = loggerContext.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(className);
        if (specialLogLevel.containsKey(className)) {
            loggerConfig.setLevel(specialLogLevel.get(className));
        } else {
            loggerConfig.setLevel(currentLogLevel);
        }
        loggerContext.updateLoggers(config);
        return new AppLogger(LogManager.getLogger(className));
    }

    public static <T> AppLogger getLogger(Class<T> clazz) {
        if (clazz == null) return new AppLogger(LogManager.getRootLogger());
        return getLogger(clazz.getName());
    }
}
