package cz.cvut.fel.pjv.warforpower;

import cz.cvut.fel.pjv.warforpower.logging.LoggingConfig;
import javafx.application.Application;

import java.util.Arrays;

public class Launcher {
    public static void main(String[] args) {
        boolean loggingEnabled = Arrays.asList(args).contains("--enable-logging");
        LoggingConfig.configure(loggingEnabled);
        Application.launch(WarForPowerApplication.class, args);
    }
}
