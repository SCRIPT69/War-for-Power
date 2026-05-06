package cz.cvut.fel.pjv.warforpower.controller;

import cz.cvut.fel.pjv.warforpower.model.game.Game;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainTile;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.view.PlayerColorCssMapper;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import cz.cvut.fel.pjv.warforpower.view.game.GameView;

import cz.cvut.fel.pjv.warforpower.view.game.TileHighlightType;
import cz.cvut.fel.pjv.warforpower.view.game.battle.BattleOverlayData;
import javafx.animation.PauseTransition;
import javafx.scene.Parent;
import javafx.application.Platform;

import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
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

    private static final int ATTACK_ENTRY_ANIMATION_MILLIS = 220;
    private static final int BATTLE_OVERLAY_DELAY_MILLIS = 700;
    private boolean battleInProgress = false;

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
        if (battleInProgress) {
            LOGGER.debug("Ignored end turn request during battle.");
            return;
        }

        LOGGER.info("End turn requested by user.");
        unitSelection.clear();

        game.endTurn();
        refreshView();
        restartTurnTimer();
    }

    /**
     * Starts a new game and initializes the first rendered state.
     */
    public void startNewGame() {
        game.startNewGame();
        LOGGER.info("New game initialization requested from controller.");

        bindViewActions();
        bindTimerActions();
        refreshView();
        restartTurnTimer();
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
                    clearTemporaryMenus();
                    refreshView();
                } catch (IllegalArgumentException | IllegalStateException e) {
                    LOGGER.warn("Unit purchase failed: {}", e.getMessage());
                }
            });
        }
    }

    /**
     * Returns whether the specified tile may currently be bought
     * by the current player.
     *
     * @param coords tile coordinates
     * @return true if the terrain tile may be bought
     */
    private boolean isPurchasableTile(HexTileCoords coords) {
        if (!(game.getGameMap().getTile(coords) instanceof TerrainTile terrainTile)) {
            return false;
        }

        return interactionRules.canBuyTerrainTile(terrainTile);
    }
    /**
     * Returns whether the specified tile is currently highlighted
     * as a valid attack target.
     *
     * @param coords tile coordinates
     * @return true if the tile is an attack-highlighted tile
     */
    private boolean isAttackTile(HexTileCoords coords) {
        return currentTileHighlights.get(coords) == TileHighlightType.ATTACK;
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
     * Returns whether the specified tile is currently interactive.
     *
     * @param coords tile coordinates
     * @return true if the tile is currently interactive
     */
    private boolean isTileInteractive(HexTileCoords coords) {
        if (battleInProgress) {
            return false;
        }

        return isMoveTile(coords)
                || isAttackTile(coords)
                || isInteractiveBaseTile(coords)
                || isPurchasableTile(coords);
    }

    /**
     * Binds timer service events to UI updates and automatic turn ending.
     */
    private void bindTimerActions() {
        timerService.setOnSucceeded(event -> {
            Integer remaining = timerService.getValue();
            if (remaining == null) {
                return;
            }

            gameView.updateTurnTimer(remaining);

            if (remaining <= 0) {
                LOGGER.info("Turn timer expired. Ending turn automatically.");

                timerService.cancel();

                Platform.runLater(this::handleEndTurn);
            }
        });
    }
    /**
     * Resets and starts countdown for the current turn.
     */
    private void restartTurnTimer() {
        timerService.cancel();
        timerService.resetTimer();
        gameView.resetTurnTimer();
        timerService.restart();
    }
    /**
     * Pauses countdown for the current turn.
     */
    private void pauseTurnTimer() {
        timerService.pause();
    }
    /**
     * Resumes countdown for the current turn.
     */
    private void resumeTurnTimer() {
        timerService.resume();
    }

    /**
     * Handles tile click input and routes it to movement, attack,
     * tile purchase, base interaction or generic deselection logic.
     *
     * @param coords clicked tile coordinates
     */
    private void handleTileClicked(HexTileCoords coords) {
        if (battleInProgress) {
            return;
        }

        if (isMoveTile(coords)){
            handleMovementClicked(coords);
            return;
        }
        if (isAttackTile(coords)) {
            handleAttackTileClicked(coords);
            return;
        }
        if (isPurchasableTile(coords)) {
            handleTilePurchaseClicked(coords);
            return;
        }
        if (isInteractiveBaseTile(coords)) {
            handleBaseClicked(coords);
            return;
        }
        handleEmptyOrNonInteractiveTileClicked();
    }

    /**
     * Handles click on a tile highlighted as a valid attack target.
     * Currently opens attack confirmation for city targets.
     *
     * @param coords clicked target tile coordinates
     */
    private void handleAttackTileClicked(HexTileCoords coords) {
        if (battleInProgress) {
            return;
        }

        ScreenPosition tilePosition = gameView.getTileScreenPosition(coords);
        ScreenPosition popupPosition = calculateConfirmationMenuPosition(tilePosition);

        clearTemporaryMenus();

        gameView.showConfirmationMenu(
                "Do you want to attack?",
                popupPosition.x(),
                popupPosition.y(),
                () -> confirmAttack(coords),
                () -> {
                    gameView.hideConfirmationMenu();
                    refreshMapLayers();
                }
        );
    }
    /**
     * Confirms attack against the specified target tile.
     *
     * @param coords attacked tile coordinates
     */
    private void confirmAttack(HexTileCoords coords) {
        gameView.hideConfirmationMenu();
        pauseTurnTimer();

        battleInProgress = true;
        gameView.setEndTurnButtonDisabled(true);

        LOGGER.info("Battle started on tile {}.", coords);

        startBattle(coords);
    }
    /**
     * Starts battle flow for the specified attacked tile.
     * Attackers first play movement animation into the target tile,
     * then battle render state is shown, and only after a short delay
     * the battle overlay is opened.
     *
     * @param coords attacked tile coordinates
     */
    private void startBattle(HexTileCoords coords) {
        if (coords == null) {
            throw new IllegalArgumentException("Battle tile coordinates cannot be null.");
        }

        List<Unit> attackers = List.copyOf(unitSelection.getSelectedUnits());
        List<Unit> defenders = List.of();

        if (game.getGameMap().getTile(coords) instanceof OccupiableTile occupiableTile) {
            defenders = List.copyOf(occupiableTile.getStandingUnits());
        }

        for (Unit attacker : attackers) {
            HexTileCoords fromCoords = attacker.getOccupiedTile().getTileCoords();
            gameView.animateUnitMovement(attacker, fromCoords, coords);
        }

        unitSelection.clear();
        refreshMapLayers();

        List<Unit> finalDefenders = defenders;

        PauseTransition entryDelay = new PauseTransition(Duration.millis(ATTACK_ENTRY_ANIMATION_MILLIS));
        entryDelay.setOnFinished(event -> {
            gameView.showBattleState(coords, attackers, finalDefenders);
            refreshMapLayers();

            LOGGER.info("Battle state shown on tile {}.", coords);

            boolean defenderIsCity = !(game.getGameMap().getTile(coords) instanceof OccupiableTile);

            BattleOverlayData overlayData = new BattleOverlayData(
                    "Battle",
                    "Attackers",
                    "Defenders",
                    attackers,
                    finalDefenders,
                    defenderIsCity
            );

            PauseTransition overlayDelay = new PauseTransition(Duration.millis(BATTLE_OVERLAY_DELAY_MILLIS));
            overlayDelay.setOnFinished(innerEvent -> {
                gameView.showBattleOverlay(overlayData, () -> {
                    gameView.hideBattleOverlay();
                    finishBattleState();
                });
            });
            overlayDelay.play();
        });
        entryDelay.play();
    }

    /**
     * Finishes current battle view state and restores normal turn controls.
     */
    private void finishBattleState() {
        battleInProgress = false;
        gameView.clearBattleState();
        gameView.setEndTurnButtonDisabled(false);
        resumeTurnTimer();
        refreshMapLayers();
    }

    /**
     * Handles click on a terrain tile that may be bought by the current player.
     *
     * @param coords clicked tile coordinates
     */
    private void handleTilePurchaseClicked(HexTileCoords coords) {
        if (battleInProgress) {
            return;
        }

        if (!(game.getGameMap().getTile(coords) instanceof TerrainTile terrainTile)) {
            throw new IllegalArgumentException("Tile purchase is only possible on terrain tiles.");
        }
        if (!interactionRules.canBuyTerrainTile(terrainTile)) {
            return;
        }

        ScreenPosition tilePosition = gameView.getTileScreenPosition(coords);
        ScreenPosition popupPosition = calculateConfirmationMenuPosition(tilePosition);

        clearTemporaryMenus();

        gameView.showConfirmationMenu(
                "Buy tile for 50 coins?",
                popupPosition.x(),
                popupPosition.y(),
                () -> confirmTilePurchase(terrainTile),
                () -> {
                    gameView.hideConfirmationMenu();
                    refreshMapLayers();
                }
        );
    }
    /**
     * Confirms terrain tile purchase.
     *
     * @param terrainTile tile to buy
     */
    private void confirmTilePurchase(TerrainTile terrainTile) {
        Player currentPlayer = game.getCurrentPlayer();

        Unit buyer = null;
        for (Unit unit : terrainTile.getStandingUnits()) {
            if (unit.getOwner() == currentPlayer) {
                buyer = unit;
                break;
            }
        }

        if (buyer == null) {
            throw new IllegalStateException("Current player has no unit on the tile to buy.");
        }

        game.captureTerrainTile(buyer, terrainTile);

        gameView.hideConfirmationMenu();
        refreshView();

        LOGGER.info("Terrain tile {} was bought by player {}.",
                terrainTile.getTileCoords(),
                currentPlayer.getDisplayLabel());
    }
    /**
     * Calculates screen position for a confirmation popup near a tile.
     *
     * @param tilePosition top-left tile screen position
     * @return popup position
     */
    private ScreenPosition calculateConfirmationMenuPosition(ScreenPosition tilePosition) {
        double menuWidth = 180;
        double menuHeight = 110;

        double menuOffsetX = 40;
        double menuOffsetY = 10;
        double screenPadding = 12;

        double menuX = tilePosition.x() + menuOffsetX;
        double menuY = tilePosition.y() + menuOffsetY;

        if (menuX + menuWidth > UIConstants.WINDOW_WIDTH - screenPadding) {
            menuX = tilePosition.x() - menuWidth + 20;
        }
        if (menuY + menuHeight > UIConstants.WINDOW_HEIGHT - screenPadding) {
            menuY = tilePosition.y() - menuHeight + 50;
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
     * Handles click on an empty or currently non-interactive tile.
     * Clears current unit/base selection and refreshes map-related visuals.
     */
    private void handleEmptyOrNonInteractiveTileClicked() {
        unitSelection.clear();
        clearTemporaryMenus();
        refreshMapLayers();
    }

    /**
     * Handles click on a tile highlighted as a valid movement target.
     * Supports movement of one selected unit or two selected units
     * to the same valid target tile.
     *
     * @param coords clicked target tile coordinates
     */
    private void handleMovementClicked(HexTileCoords coords) {
        int selectedCount = unitSelection.size();
        if (selectedCount != 1 && selectedCount != 2) {
            return;
        }

        clearTemporaryMenus();

        if (!(game.getGameMap().getTile(coords) instanceof OccupiableTile tile)) {
            throw new IllegalArgumentException("Cannot move to a non-occupiable tile.");
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
        if (battleInProgress) {
            return;
        }

        clearTemporaryMenus();
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
     * Clears temporary popup/menu state.
     */
    private void clearTemporaryMenus() {
        selectedBaseCoords = null;
        gameView.hidePurchaseMenu();
        gameView.hideConfirmationMenu();
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
        if (battleInProgress) {
            return;
        }
        if (unit == null) {
            return;
        }

        HexTileCoords occupiedCoords = unit.getOccupiedTile().getTileCoords();

        if (unit.getOwner() != game.getCurrentPlayer() && isAttackTile(occupiedCoords)) {
            handleAttackTileClicked(occupiedCoords);
            return;
        }

        if (!interactionRules.isUnitInteractive(unit)
                && !isPurchasableTile(occupiedCoords)) {
            LOGGER.debug("Ignored click on non-interactive unit {}.", unit);
            return;
        }

        if (!shiftHeld
                && unitSelection.hasSelection()
                && unitSelection.getSelectedUnits().contains(unit)
                && isPurchasableTile(occupiedCoords)) {
            handleTilePurchaseClicked(occupiedCoords);
            return;
        }

        if (!interactionRules.isUnitInteractive(unit)) {
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

        clearTemporaryMenus();

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

        clearTemporaryMenus();
        gameView.setEndTurnButtonDisabled(battleInProgress);
        refreshMapLayers();
    }
}
