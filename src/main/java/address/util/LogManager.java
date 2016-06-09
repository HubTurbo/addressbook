package address.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.LoggerFactory;

public class LogManager {
    public static Logger getLogger(String className) {
        Logger namedLogger = (Logger) LoggerFactory.getLogger(className);

        LoggerContext loggerContext = getResetContext(namedLogger);
        PatternLayoutEncoder encoder = createPatternEncoder(loggerContext);
        ConsoleAppender<ILoggingEvent> appender = createAppender(loggerContext, encoder);

        namedLogger.addAppender(appender);
        return namedLogger;
    }

    private static ConsoleAppender<ILoggingEvent> createAppender(LoggerContext loggerContext, PatternLayoutEncoder encoder) {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(loggerContext);
        appender.setEncoder(encoder);
        appender.start();
        return appender;
    }

    private static PatternLayoutEncoder createPatternEncoder(LoggerContext loggerContext) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%magenta(%-30.-30([%thread]))| %highlight(%.-1level) %yellow(%d{HH:mm:ss.SSS}) %gray(%30.30logger{0}) %message%n");
        encoder.start();
        return encoder;
    }

    private static LoggerContext getResetContext(Logger namedLogger) {
        LoggerContext loggerContext = namedLogger.getLoggerContext();
        loggerContext.reset();
        return loggerContext;
    }
}
