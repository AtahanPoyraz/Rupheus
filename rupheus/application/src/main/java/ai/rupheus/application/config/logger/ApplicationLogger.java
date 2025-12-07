package ai.rupheus.application.config.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ApplicationLogger {
    public Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }

    public void info(Class<?> clazz, String message) {
        getLogger(clazz).info(message);
    }

    public void warn(Class<?> clazz, String message) {
        getLogger(clazz).warn(message);
    }

    public void error(Class<?> clazz, String message) {
        getLogger(clazz).error(message);
    }

    public void error(Class<?> clazz, String message, Throwable throwable) {
        getLogger(clazz).error(message, throwable);
    }

    public void debug(Class<?> clazz, String message) {
        getLogger(clazz).debug(message);
    }
}
