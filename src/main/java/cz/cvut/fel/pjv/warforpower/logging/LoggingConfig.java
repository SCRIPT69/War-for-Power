package cz.cvut.fel.pjv.warforpower.logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configures application-wide logging.
 */
public final class LoggingConfig {
    private LoggingConfig() {
    }

    /**
     * Enables or disables logging output for the whole application.
     *
     * @param enabled true to enable logging, false to disable it
     */
    public static void configure(boolean enabled) {
        Logger rootLogger = Logger.getLogger("");
        Level level = enabled ? Level.INFO : Level.OFF;

        rootLogger.setLevel(level);
        for (Handler handler : rootLogger.getHandlers()) {
            handler.setLevel(level);
        }
    }
}