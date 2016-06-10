package address.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import java.util.HashMap;

public class LoggerManager {
    public static Level currentLogLevel;
    public static HashMap<String, Level> specialLogLevel;

    public static Logger getLogger(String className, Level loggingLevel) {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration config = loggerContext.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(className);
        loggerConfig.setLevel(loggingLevel);
        return LogManager.getLogger(className);
    }

    public static Logger getLogger(String className) {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        Configuration config = loggerContext.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(className);

        if (specialLogLevel.containsKey(className)) {
            loggerConfig.setLevel(specialLogLevel.get(className));
        } else {
            loggerConfig.setLevel(currentLogLevel);
        }
        return LogManager.getLogger(className);
    }

    public static <T> Logger getLogger(Class<T> clazz) {
        if (clazz == null) return LogManager.getRootLogger();
        return LogManager.getLogger(clazz.getName());
    }
}
