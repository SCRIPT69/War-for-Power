package cz.cvut.fel.pjv.warforpower.view;

import cz.cvut.fel.pjv.warforpower.controller.GameController;
import cz.cvut.fel.pjv.warforpower.model.game.Game;
import cz.cvut.fel.pjv.warforpower.save.SaveManager;
import cz.cvut.fel.pjv.warforpower.view.menu.MenuView;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {
    private final Stage stage;

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void openMenuScene() {
        MenuView menuView = new MenuView(this, SaveManager.hasSave());
        Scene scene = new Scene(menuView.getRoot(), UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);

        scene.getStylesheets().add(
                getClass().getResource("/styles/menu.css").toExternalForm()
        );

        stage.setScene(scene);
    }

    public void openGameScene(int selectedPlayersCount) {
        GameController gameController = new GameController(selectedPlayersCount, this);
        Scene scene = new Scene(gameController.getGameViewRoot(), UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);

        stage.setScene(scene);
        gameController.startNewGame();
    }

    /**
     * Opens game scene with an already restored game instance.
     *
     * @param game restored game to continue
     */
    public void openGameScene(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }

        GameController gameController = new GameController(game, this);
        Scene scene = new Scene(gameController.getGameViewRoot(), UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);

        stage.setScene(scene);
        gameController.startLoadedGame();
    }
}