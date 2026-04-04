package cz.cvut.fel.pjv.warforpower.view.game;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Main in-game view container combining the map view,
 * top panel and additional UI overlays.
 */
public class GameView {
    private final AnchorPane root = new AnchorPane();

    private final GameMapView gameMapView;
    private final GameTopPanelView topPanelView =
            new GameTopPanelView(UIConstants.WINDOW_WIDTH - 80);
    private final UnitPurchaseMenuView purchaseMenuView = new UnitPurchaseMenuView();

    public GameView(GameMap gameMap) {
        gameMapView = new GameMapView(gameMap);

        AnchorPane.setTopAnchor(gameMapView.getCanvas(), 0.0);
        AnchorPane.setLeftAnchor(gameMapView.getCanvas(), 0.0);

        AnchorPane.setTopAnchor(topPanelView.getRoot(), 22.0);
        AnchorPane.setLeftAnchor(topPanelView.getRoot(), 40.0);
        AnchorPane.setRightAnchor(topPanelView.getRoot(), 40.0);

        root.getChildren().addAll(gameMapView.getCanvas(), topPanelView.getRoot(), purchaseMenuView.getRoot());
    }

    public Parent getRoot() {
        return root;
    }

    public void renderMap() {
        gameMapView.renderMap();
    }

    public void setOnTileClicked(Consumer<HexTileCoords> onTileClicked) {
        gameMapView.setOnTileClicked(onTileClicked);
    }

    public void setTileInteractivePredicate(Predicate<HexTileCoords> isTileInteractive) {
        gameMapView.setTileInteractivePredicate(isTileInteractive);
    }

    public ScreenPosition getTileScreenPosition(HexTileCoords coords) {
        return gameMapView.getTileScreenPosition(coords);
    }

    public void addHighlightedTile(HexTileCoords coords) {
        gameMapView.addHighlightedTile(coords);
    }

    public void clearHighlightedTiles() {
        gameMapView.clearHighlightedTiles();
    }

    public void updateTopPanel(String playerName, String playerColorCss, int coins, int round) {
        topPanelView.update(playerName, playerColorCss, coins, round);
    }

    public Button getEndTurnButton() {
        return topPanelView.getEndTurnButton();
    }

    public void showPurchaseMenuAt(double x, double y) {
        AnchorPane.setLeftAnchor(purchaseMenuView.getRoot(), x);
        AnchorPane.setTopAnchor(purchaseMenuView.getRoot(), y);
        purchaseMenuView.show();
    }
    public void hidePurchaseMenu() {
        purchaseMenuView.hide();
    }
    public Button getBuyUnitButton(UnitType unitType) {
        return purchaseMenuView.getBuyButton(unitType);
    }
}