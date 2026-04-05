package cz.cvut.fel.pjv.warforpower;

import cz.cvut.fel.pjv.warforpower.view.SceneManager;
import javafx.application.Application;

import javafx.stage.Stage;

import java.io.IOException;

public class WarForPowerApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager sceneManager = new SceneManager(stage);
        sceneManager.openMenuScene();
        stage.setTitle("War for Power");
        stage.show();
    }
}
