package address.util;

import address.events.BaseEvent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;

public class AppLogger {
    private Logger logger;

    public AppLogger(Logger logger) {
        this.logger = logger;
    }

    public void info(String message, Object... params) {
        logger.info(message, params);
    }

    public void debug(String message, Object... params) {
        logger.debug(message, params);
    }

    public void trace(String message, Object... params) {
        logger.trace(message, params);
    }

    public void warn(String message, Object... params) {
        logger.warn(message, params);

    }

    public void error(String message, Object... params) {
        logger.error(message, params);
    }

    public void fatal(String message, Object... params) {
        logger.fatal(message, params);
    }

    public <T extends Throwable> void throwing(T throwable) {
        logger.throwing(Level.DEBUG, throwable);
    }

    public <T extends Throwable> void catching(T throwable) {
        logger.catching(Level.DEBUG, throwable);
    }

    public void debugEvent(BaseEvent event) {
        logger.debug("{}: {}", event.getClass().getName(), event.toString());
    }

    public void infoEvent(BaseEvent event) {
        logger.info("{}: {}", event.getClass().getName(), event.toString());
    }

    public void traceEntry(Object... params) {
        logger.traceEntry("{}", params);
    }

    public <R> void traceExit(R result) {
        logger.traceExit(result);
    }
}
