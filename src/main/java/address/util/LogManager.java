package address.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import org.slf4j.LoggerFactory;

public class LogManager {
    public static <T> Logger getLogger(Class<T> clazz) {
        Logger namedLogger = (Logger) LoggerFactory.getLogger(clazz.getName());

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
        encoder.setPattern("%magenta(%-18.-18([%thread]))|%yellow(%13.13d{HH:mm:ss.SSS}) %highlight(%.-1level) %gray(%-15.-15logger{0}) %message%n");
        encoder.start();
        return encoder;
    }

    private static LoggerContext getResetContext(Logger namedLogger) {
        LoggerContext loggerContext = namedLogger.getLoggerContext();
        loggerContext.reset();
        return loggerContext;
    }
}
