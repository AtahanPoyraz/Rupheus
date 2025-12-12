package ai.rupheus.application.common.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApplicationLogger {
    public Logger getLogger(Class<?> source) {
        return LoggerFactory.getLogger(source);
    }

    public void info(Class<?> source, String message) {
        getLogger(source).info(message);
    }

    public void warn(Class<?> source, String message) {
        getLogger(source).warn(message);
    }

    public void error(Class<?> source, String message) {
        getLogger(source).error(message);
    }

    public void error(Class<?> source, String message, Throwable throwable) {
        getLogger(source).error(message, throwable);
    }

    public void debug(Class<?> source, String message) {
        getLogger(source).debug(message);
    }
}
