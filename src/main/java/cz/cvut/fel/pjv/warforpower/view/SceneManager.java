package cz.cvut.fel.pjv.warforpower.view;

import cz.cvut.fel.pjv.warforpower.controller.GameController;
import cz.cvut.fel.pjv.warforpower.view.menu.MenuView;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    private final Stage stage;

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void openMenuScene() {
        MenuView menuView = new MenuView(this);
        Scene scene = new Scene(menuView.getRoot(), UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        scene.getStylesheets().add(
                getClass().getResource("/styles/menu.css").toExternalForm()
        );
        stage.setScene(scene);
    }

    public void openGameScene(int selectedPlayersCount) {
        GameController gameController = new GameController(selectedPlayersCount);
        Scene scene = new Scene(gameController.getGameViewRoot(), UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        stage.setScene(scene);
        gameController.startNewGame();
    }
}
