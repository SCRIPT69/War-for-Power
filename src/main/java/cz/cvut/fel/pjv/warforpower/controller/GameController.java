package cz.cvut.fel.pjv.warforpower.controller;

import cz.cvut.fel.pjv.warforpower.model.game.Game;
import cz.cvut.fel.pjv.warforpower.view.game.GameView;
import javafx.scene.Parent;

public class GameController {
    private final Game game;
    private final GameView gameView;

    public GameController(int playersNumber) {
        this.game = new Game(playersNumber);
        this.gameView = new GameView();
    }

    public Parent getGameViewRoot() {
        return this.gameView.getRoot();
    }

    public void startNewGame() {
        game.startNewGame();
        gameView.renderMap(game.getGameMap());
    }
}
