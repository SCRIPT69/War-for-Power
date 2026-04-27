package cz.cvut.fel.pjv.warforpower.controller;

import cz.cvut.fel.pjv.warforpower.model.game.Game;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.view.PlayerColorCssMapper;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import cz.cvut.fel.pjv.warforpower.view.game.GameView;

import cz.cvut.fel.pjv.warforpower.view.game.TileHighlightType;
import javafx.scene.Parent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Connects user input from the game view with the game model
 * and updates the visible game state accordingly.
 */
public class GameController {
    private final Game game;
    private final GameView gameView;
    private HexTileCoords selectedBaseCoords;
    private final InteractionRules interactionRules;
    private final UnitSelection unitSelection;

    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    private final TurnTimerService timerService;
    private final SelectionTileHighlightResolver tileHighlightResolver;
    private Map<HexTileCoords, TileHighlightType> currentTileHighlights;

    /**
     * Creates a controller for a new game with the given number of players.
     *
     * @param playersNumber number of players
     */
    public GameController(int playersNumber) {
        this.game = new Game(playersNumber);
        this.gameView = new GameView(game.getGameMap());
        this.interactionRules = new InteractionRules(this.game);
        this.unitSelection = new UnitSelection();
        this.timerService = new TurnTimerService();
        this.tileHighlightResolver = new SelectionTileHighlightResolver(game);
        this.currentTileHighlights = new HashMap<>();
    }

    /**
     * Returns the root node of the game view.
     *
     * @return root UI node
     */
    public Parent getGameViewRoot() {
        return this.gameView.getRoot();
    }

    /**
     * Ends the current player's turn and refreshes the visible game state.
     */
    private void handleEndTurn() {
        LOGGER.info("End turn requested by user.");
        unitSelection.clear();
        game.endTurn();
        refreshView();
    }

    /**
     * Starts a new game and initializes the first rendered state.
     */
    public void startNewGame() {
        game.startNewGame();
        LOGGER.info("New game initialization requested from controller.");

        bindViewActions();
        refreshView();
    }

    /**
     * Binds game view actions to controller handlers.
     */
    private void bindViewActions() {
        // End turn button
        gameView.getEndTurnButton().setOnAction(event -> handleEndTurn());

        gameView.setOnUnitClicked(this::handleUnitClicked);
        gameView.setUnitInteractivePredicate(interactionRules::isUnitInteractive);

        // Tile clicks are handled centrally by the controller
        gameView.setOnTileClicked(this::handleTileClicked);
        gameView.setTileInteractivePredicate(this::isTileInteractive);

        // Recruitment buttons attempt to buy a unit on the currently selected base
        for (UnitType unitType : UnitType.values()) {
            gameView.getBuyUnitButton(unitType).setOnAction(event -> {
                if (selectedBaseCoords == null) {
                    return;
                }

                try {
                    game.buyUnit(unitType, selectedBaseCoords);
                    clearBaseSelection();
                    refreshView();
                } catch (IllegalArgumentException | IllegalStateException e) {
                    LOGGER.warn("Unit purchase failed: {}", e.getMessage());
                }
            });
        }
    }
    /**
     * Returns whether the specified tile is currently interactive.
     * A tile is interactive if it is highlighted as a movement tile,
     * or if it is an interactive base.
     *
     * @param coords tile coordinates
     * @return true if the tile is currently interactive
     */
    private boolean isTileInteractive(HexTileCoords coords) {
        return isMoveTile(coords) || isInteractiveBaseTile(coords);
    }

    /**
     * Handles tile click input and routes it to movement, base interaction,
     * or generic deselection logic depending on the clicked tile.
     *
     * @param coords clicked tile coordinates
     */
    private void handleTileClicked(HexTileCoords coords) {
        if (isMoveTile(coords)){
            handleMovementClicked(coords);
            return;
        }
        else if (isInteractiveBaseTile(coords)) {
            handleBaseClicked(coords);
            return;
        }
        handleEmptyOrNonInteractiveTileClicked();
    }

    /**
     * Returns whether the specified tile is currently highlighted
     * as a valid movement target.
     *
     * @param coords tile coordinates
     * @return true if the tile is a movement-highlighted tile
     */
    private boolean isMoveTile(HexTileCoords coords) {
        return currentTileHighlights.get(coords) == TileHighlightType.MOVE;
    }

    /**
     * Returns whether the specified tile is an interactive base
     * of the current player.
     *
     * @param coords tile coordinates
     * @return true if the tile is an interactive base
     */
    private boolean isInteractiveBaseTile(HexTileCoords coords) {
        if (!(game.getGameMap().getTile(coords) instanceof BaseTile baseTile)) {
            return false;
        }
        return interactionRules.isBaseInteractive(baseTile);
    }

    /**
     * Handles click on an empty or currently non-interactive tile.
     * Clears current unit/base selection and refreshes map-related visuals.
     */
    private void handleEmptyOrNonInteractiveTileClicked() {
        unitSelection.clear();
        clearBaseSelection();
        refreshMapLayers();
    }

    /**
     * Handles click on a tile highlighted as a valid movement target.
     * Currently supports movement of exactly one selected unit.
     *
     * @param coords clicked target tile coordinates
     */
    private void handleMovementClicked(HexTileCoords coords) {
        int selectedCount = unitSelection.size();
        if (selectedCount != 1 && selectedCount != 2) {
            return;
        }

        clearBaseSelection();

        if (!(game.getGameMap().getTile(coords) instanceof OccupiableTile tile)) {
            throw new IllegalArgumentException("Can not move on not occupiable tile");
        }

        if (selectedCount == 1) {
            Unit selectedUnit = unitSelection.getFirstSelectedUnit();
            HexTileCoords fromCoords = selectedUnit.getOccupiedTile().getTileCoords();

            game.moveUnitToTile(selectedUnit, tile);
            gameView.animateUnitMovement(selectedUnit, fromCoords, coords);
        }
        else {
            Unit first = unitSelection.getSelectedUnits().getFirst();
            Unit second = unitSelection.getSelectedUnits().get(1);

            HexTileCoords firstFrom = first.getOccupiedTile().getTileCoords();
            HexTileCoords secondFrom = second.getOccupiedTile().getTileCoords();

            game.moveUnitsToTile(first, second, tile);

            gameView.animateUnitMovement(first, firstFrom, coords);
            gameView.animateUnitMovement(second, secondFrom, coords);
        }

        unitSelection.clear();
        refreshMapLayers();
    }

    /**
     * Handles click on an interactive base tile.
     * Clears current unit selection, refreshes map visuals
     * and opens the purchase menu for the selected base.
     *
     * @param coords clicked base tile coordinates
     */
    private void handleBaseClicked(HexTileCoords coords) {
        clearBaseSelection();
        unitSelection.clear();
        refreshMapLayers();

        selectedBaseCoords = coords;
        ScreenPosition position = calculatePurchaseMenuPosition(gameView.getTileScreenPosition(coords));
        gameView.showPurchaseMenuAt(position.x(), position.y());

        LOGGER.info("Purchase menu opened for base {}.", coords);
    }
    /**
     * Calculates a screen position for the purchase menu near the selected base tile.
     * The menu is automatically shifted to stay within the visible window bounds.
     *
     * @param tilePosition top-left screen position of the selected tile
     * @return adjusted screen position of the purchase menu
     */
    private ScreenPosition calculatePurchaseMenuPosition(ScreenPosition tilePosition) {
        double purchaseMenuWidth = 204;
        double purchaseMenuHeight = 205;

        double menuOffsetX = 55;
        double menuOffsetY = 10;
        double screenPadding = 12;

        double menuX = tilePosition.x() + menuOffsetX;
        double menuY = tilePosition.y() + menuOffsetY;

        if (menuX + purchaseMenuWidth > UIConstants.WINDOW_WIDTH - screenPadding) {
            menuX = tilePosition.x() - purchaseMenuWidth + 20;
        }
        if (menuY + purchaseMenuHeight > UIConstants.WINDOW_HEIGHT - screenPadding) {
            menuY = tilePosition.y() - purchaseMenuHeight + 70;
        }
        if (menuX < screenPadding) {
            menuX = screenPadding;
        }
        if (menuY < screenPadding) {
            menuY = screenPadding;
        }

        return new ScreenPosition(menuX, menuY);
    }
    /**
     * Updates map-related visuals after selection or highlight state change
     * without closing the currently visible purchase menu.
     */
    private void refreshMapLayers() {
        updateTileHighlights();
        gameView.renderMap();
        gameView.renderUnits(game.getGameMap(), unitSelection.getSelectedUnits());
    }

    /**
     * Clears current base selection and hides the purchase menu.
     */
    private void clearBaseSelection() {
        selectedBaseCoords = null;
        gameView.hidePurchaseMenu();
    }

    /**
     * Updates visible tile highlights according to current selection state.
     */
    private void updateTileHighlights() {
        gameView.clearTileHighlights();

        currentTileHighlights =
                tileHighlightResolver.resolve(unitSelection, game.getCurrentPlayer(), game.getCurrentRound());

        for (Map.Entry<HexTileCoords, TileHighlightType> entry : currentTileHighlights.entrySet()) {
            gameView.addTileHighlight(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Handles unit click input and updates current unit selection.
     * Shift-click may add a second unit if both selected units may act
     * on at least one common tile.
     *
     * @param unit clicked unit
     * @param shiftHeld true if shift was held during click
     */
    private void handleUnitClicked(Unit unit, boolean shiftHeld) {
        if (unit == null) {
            return;
        }
        if (!interactionRules.isUnitInteractive(unit)) {
            LOGGER.debug("Ignored click on non-interactive unit {}.", unit);
            return;
        }

        if (!shiftHeld) {
            unitSelection.selectSingle(unit);
        } else {
            if (!unitSelection.hasSelection()) {
                unitSelection.selectSingle(unit);
            } else if (unitSelection.size() == 1) {
                Unit firstSelected = unitSelection.getFirstSelectedUnit();

                if (firstSelected == unit) {
                    unitSelection.selectSingle(unit);
                } else if (interactionRules.canUnitsBeSelectedTogether(firstSelected, unit)) {
                    unitSelection.selectPair(firstSelected, unit);
                } else {
                    unitSelection.selectSingle(unit);
                }
            } else {
                unitSelection.selectSingle(unit);
            }
        }

        clearBaseSelection();

        LOGGER.info("Selected {} unit(s).", unitSelection.size());
        refreshView();
    }

    /**
     * Refreshes the main game view according to the current game state.
     * Updates top panel data, highlighted tiles and rendered map layers.
     */
    private void refreshView() {
        Player currentPlayer = game.getCurrentPlayer();

        gameView.updateTopPanel(
                currentPlayer.getName(),
                PlayerColorCssMapper.toCssColor(currentPlayer.getColor()),
                currentPlayer.getMoney(),
                game.getCurrentRound()
        );

        // Close temporary UI from the previous turn and redraw map layers.
        gameView.hidePurchaseMenu();
        refreshMapLayers();
    }
}
