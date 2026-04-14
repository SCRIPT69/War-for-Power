package cz.cvut.fel.pjv.warforpower.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

/**
 * Configures application-wide logging level at runtime.
 */
public final class LoggingConfig {
    private LoggingConfig() {
    }

    /**
     * Enables or disables logging output for the whole application.
     * When enabled, sets root logging level to INFO. When disabled, sets it to OFF.
     *
     * @param enabled true to enable logging, false to disable it
     */
    public static void configure(boolean enabled) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger root = context.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(enabled ? Level.INFO : Level.OFF);
    }
}