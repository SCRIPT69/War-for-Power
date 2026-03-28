package cz.cvut.fel.pjv.warforpower.view.game;

import cz.cvut.fel.pjv.warforpower.model.game.Game;
import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

public class GameView {
    private final AnchorPane root = new AnchorPane();

    private final GameMapView gameMapView = new GameMapView();
    private final GameTopPanelView topPanelView =
            new GameTopPanelView(UIConstants.WINDOW_WIDTH - 80);

    public GameView(Game game) {
        AnchorPane.setTopAnchor(gameMapView.getCanvas(), 0.0);
        AnchorPane.setLeftAnchor(gameMapView.getCanvas(), 0.0);

        AnchorPane.setTopAnchor(topPanelView.getRoot(), 22.0);
        AnchorPane.setLeftAnchor(topPanelView.getRoot(), 40.0);
        AnchorPane.setRightAnchor(topPanelView.getRoot(), 40.0);

        root.getChildren().addAll(gameMapView.getCanvas(), topPanelView.getRoot());
    }

    public Parent getRoot() {
        return root;
    }

    public void renderMap(GameMap gameMap) {
        gameMapView.renderMap(gameMap);
    }

    public void updateTopPanel(String playerName, String playerColorCss, int coins, int round) {
        topPanelView.update(playerName, playerColorCss, coins, round);
    }

    public Button getEndTurnButton() {
        return topPanelView.getEndTurnButton();
    }
}