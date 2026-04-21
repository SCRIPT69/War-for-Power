package cz.cvut.fel.pjv.warforpower.controller;

import cz.cvut.fel.pjv.warforpower.model.game.Game;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.view.PlayerColorCssMapper;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import cz.cvut.fel.pjv.warforpower.view.game.GameView;

import javafx.scene.Parent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Connects user input from the game view with the game model
 * and updates the visible game state accordingly.
 */
public class GameController {
    private final Game game;
    private final GameView gameView;
    private HexTileCoords selectedBaseCoords;
    private final InteractionRules interactionRules;
    private final UnitSelection unitSelection = new UnitSelection();

    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    private final TurnTimerService timerService;

    /**
     * Creates a controller for a new game with the given number of players.
     *
     * @param playersNumber number of players
     */
    public GameController(int playersNumber) {
        this.game = new Game(playersNumber);
        this.gameView = new GameView(game.getGameMap());
        this.interactionRules = new InteractionRules(this.game);
        this.timerService = new TurnTimerService();
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

        // Only bases of the current player are treated as interactive tiles
        gameView.setTileInteractivePredicate(coords -> {
            if (!(game.getGameMap().getTile(coords) instanceof BaseTile baseTile)) {
                return false;
            }
            return interactionRules.isBaseInteractive(baseTile);
        });

        // Recruitment buttons attempt to buy a unit on the currently selected base
        for (UnitType unitType : UnitType.values()) {
            gameView.getBuyUnitButton(unitType).setOnAction(event -> {
                if (selectedBaseCoords == null) {
                    return;
                }

                try {
                    game.buyUnit(unitType, selectedBaseCoords);
                    gameView.hidePurchaseMenu();
                    selectedBaseCoords = null;
                    refreshView();
                } catch (IllegalArgumentException | IllegalStateException e) {
                    LOGGER.warn("Unit purchase failed: {}", e.getMessage());
                }
            });
        }
    }
    /**
     * Handles tile click input and opens or closes the purchase menu
     * depending on whether the clicked tile is currently interactive.
     *
     * @param coords clicked tile coordinates
     */
    private void handleTileClicked(HexTileCoords coords) {
        unitSelection.clear();

        if (!(game.getGameMap().getTile(coords) instanceof BaseTile baseTile)) {
            selectedBaseCoords = null;
            gameView.hidePurchaseMenu();
            refreshView();
            return;
        }

        if (!interactionRules.isBaseInteractive(baseTile)) {
            selectedBaseCoords = null;
            gameView.hidePurchaseMenu();
            refreshView();
            return;
        }

        selectedBaseCoords = coords;

        ScreenPosition position = calculatePurchaseMenuPosition(gameView.getTileScreenPosition(coords));
        gameView.showPurchaseMenuAt(position.x(), position.y());

        // Only redraw map/unit layers to remove unit selection highlight.
        gameView.renderMap();
        gameView.renderUnits(game.getGameMap(), unitSelection.getSelectedUnits());

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
     * Handles unit click input and updates current unit selection.
     * Shift-click may add a second unit if both selected units share
     * at least one common valid movement target.
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
                } else if (canUnitsBeSelectedTogether(firstSelected, unit)) {
                    unitSelection.selectPair(firstSelected, unit);
                } else {
                    unitSelection.selectSingle(unit);
                }
            } else {
                unitSelection.selectSingle(unit);
            }
        }

        selectedBaseCoords = null;
        gameView.hidePurchaseMenu();

        LOGGER.info("Selected {} unit(s).", unitSelection.size());
        refreshView();
    }
    /**
     * Returns whether two units may be selected together.
     * Two units are considered compatible if they share at least one
     * common valid movement target.
     *
     * @param first first unit
     * @param second second unit
     * @return true if both units share a common movement target
     */
    private boolean canUnitsBeSelectedTogether(Unit first, Unit second) {
        List<OccupiableTile> firstOptions = game.getMovementOptions(first);
        List<OccupiableTile> secondOptions = game.getMovementOptions(second);

        for (OccupiableTile firstTile : firstOptions) {
            if (secondOptions.contains(firstTile)) {
                return true;
            }
        }
        return false;
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

        gameView.clearHighlightedTiles();
        // Highlight bases of the newly active player.
        if (game.getCurrentRound() == 1) {
            BaseTile currentPlayerBase = game.getGameMap().getBasesOfPlayer(currentPlayer).getFirst();
            gameView.addHighlightedTile(currentPlayerBase.getTileCoords());
        }

        // Close temporary UI from the previous turn and redraw map layers.
        gameView.hidePurchaseMenu();
        gameView.renderMap();
        gameView.renderUnits(game.getGameMap(), unitSelection.getSelectedUnits());
    }
}
