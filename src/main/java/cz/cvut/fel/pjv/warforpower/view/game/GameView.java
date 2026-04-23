package cz.cvut.fel.pjv.warforpower.view.game;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;
import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import java.util.List;
import java.util.function.BiConsumer;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import cz.cvut.fel.pjv.warforpower.view.game.unit.UnitLayerView;
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
    private final UnitLayerView unitLayerView = new UnitLayerView();
    private final GameTopPanelView topPanelView =
            new GameTopPanelView(UIConstants.WINDOW_WIDTH - 80);
    private final UnitPurchaseMenuView purchaseMenuView = new UnitPurchaseMenuView();
    private final GameMap gameMap;

    public GameView(GameMap gameMap) {
        this.gameMap = gameMap;
        gameMapView = new GameMapView(this.gameMap);

        AnchorPane.setTopAnchor(gameMapView.getCanvas(), 0.0);
        AnchorPane.setLeftAnchor(gameMapView.getCanvas(), 0.0);

        AnchorPane.setTopAnchor(topPanelView.getRoot(), 22.0);
        AnchorPane.setLeftAnchor(topPanelView.getRoot(), 40.0);
        AnchorPane.setRightAnchor(topPanelView.getRoot(), 40.0);

        root.getChildren().addAll(gameMapView.getCanvas(),
                unitLayerView.getCanvas(),
                topPanelView.getRoot(),
                purchaseMenuView.getRoot());
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

    /**
     * Adds tile highlight of the specified type.
     *
     * @param coords tile coordinates
     * @param type highlight type
     */
    public void addTileHighlight(HexTileCoords coords, TileHighlightType type) {
        gameMapView.addTileHighlight(coords, type);
    }

    /**
     * Clears all tile highlights.
     */
    public void clearTileHighlights() {
        gameMapView.clearTileHighlights();
    }


    /**
     * Registers predicate deciding whether a unit should be treated as interactive.
     *
     * @param isUnitInteractive predicate for unit interactivity
     */
    public void setUnitInteractivePredicate(Predicate<Unit> isUnitInteractive) {
        gameMapView.setUnitInteractivePredicate(isUnitInteractive);
    }

    /**
     * Registers unit click handler.
     *
     * @param onUnitClicked callback receiving clicked unit and shift key state
     */
    public void setOnUnitClicked(BiConsumer<Unit, Boolean> onUnitClicked) {
        gameMapView.setOnUnitClicked(onUnitClicked);
        gameMapView.setUnitAtResolver((mouseX, mouseY) ->
                unitLayerView.findUnitAt(gameMap, mouseX, mouseY));
    }


    public void updateTopPanel(String playerName, String playerColorCss, int coins, int round) {
        topPanelView.update(playerName, playerColorCss, coins, round);
    }

    /**
     * Renders units on the unit layer and highlights currently selected units.
     *
     * @param gameMap current game map
     * @param selectedUnits currently selected units
     */
    public void renderUnits(GameMap gameMap, List<Unit> selectedUnits) {
        unitLayerView.setSelectedUnits(selectedUnits);
        unitLayerView.renderUnits(gameMap);
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