package cz.cvut.fel.pjv.warforpower.controller;

import cz.cvut.fel.pjv.warforpower.model.game.Game;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;
import cz.cvut.fel.pjv.warforpower.view.PlayerColorCssMapper;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import cz.cvut.fel.pjv.warforpower.view.game.GameView;
import javafx.scene.Parent;

/**
 * Connects user input from the game view with the game model
 * and updates the visible game state accordingly.
 */
public class GameController {
    private final Game game;
    private final GameView gameView;
    private HexTileCoords selectedBaseCoords;
    private final TurnTimerService timerService;

    /**
     * Creates a controller for a new game with the given number of players.
     *
     * @param playersNumber number of players
     */
    public GameController(int playersNumber) {
        this.game = new Game(playersNumber);
        this.gameView = new GameView(game.getGameMap());
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
     * Ends the current player's turn, switches to the next active player
     * and refreshes the visible game state.
     */
    private void handleEndTurn() {
        game.endTurn();

        Player currentPlayer = game.getCurrentPlayer();

        gameView.updateTopPanel(
                currentPlayer.getName(),
                PlayerColorCssMapper.toCssColor(currentPlayer.getColor()),
                currentPlayer.getMoney(),
                game.getCurrentRound()
        );

        // Highlight bases of the newly active player.
        gameView.clearHighlightedTiles();
        for (BaseTile base : game.getGameMap().getBasesOfPlayer(currentPlayer)) {
            gameView.addHighlightedTile(base.getTileCoords());
        }

        // Close temporary UI from the previous turn and redraw map layers.
        gameView.hidePurchaseMenu();
        gameView.renderMap();
    }

    /**
     * Starts a new game and initializes the first rendered state.
     */
    public void startNewGame() {
        game.startNewGame();
        bindViewActions();
        refreshView();
    }

    /**
     * Binds game view actions to controller handlers.
     */
    private void bindViewActions() {
        // End turn button
        gameView.getEndTurnButton().setOnAction(event -> handleEndTurn());

        // Tile clicks are handled centrally by the controller
        gameView.setOnTileClicked(this::handleTileClicked);

        // Only bases of the current player are treated as interactive tiles
        gameView.setTileInteractivePredicate(coords -> {
            if (!(game.getGameMap().getTile(coords) instanceof BaseTile baseTile)) {
                return false;
            }
            return baseTile.getOwner() == game.getCurrentPlayer();
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
                    System.out.println(e.getMessage());
                }
            });
        }
    }
    /**
     * Handles tile click input and opens or closes the purchase menu
     * depending on whether the clicked tile is the current player's base.
     *
     * @param coords clicked tile coordinates
     */
    private void handleTileClicked(HexTileCoords coords) {
        if (!(game.getGameMap().getTile(coords) instanceof BaseTile baseTile)) {
            selectedBaseCoords = null;
            gameView.hidePurchaseMenu();
            return;
        }

        if (baseTile.getOwner() != game.getCurrentPlayer()) {
            selectedBaseCoords = null;
            gameView.hidePurchaseMenu();
            return;
        }

        selectedBaseCoords = coords;

        ScreenPosition position = calculatePurchaseMenuPosition(gameView.getTileScreenPosition(coords));
        gameView.showPurchaseMenuAt(position.x(), position.y());
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

        BaseTile currentPlayerBase = game.getGameMap().getBasesOfPlayer(currentPlayer).getFirst();
        gameView.addHighlightedTile(currentPlayerBase.getTileCoords());

        gameView.renderMap();
        gameView.renderUnits(game.getGameMap());
    }
}
