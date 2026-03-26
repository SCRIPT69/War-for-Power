package cz.cvut.fel.pjv.warforpower.view.game;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

public class GameView {
    private final StackPane root = new StackPane();
    private final GameMapView gameMapView = new GameMapView();

    public Parent getRoot() {
        return root;
    }

    public GameView() {
        this.root.getChildren().addAll(gameMapView.getCanvas());
    }

    public void renderMap(GameMap gameMap) {
        gameMapView.renderMap(gameMap);
    }
}
