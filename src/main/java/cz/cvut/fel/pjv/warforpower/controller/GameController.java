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

public class GameController {
    private final Game game;
    private final GameView gameView;
    private HexTileCoords selectedBaseCoords;

    public GameController(int playersNumber) {
        this.game = new Game(playersNumber);
        this.gameView = new GameView(game.getGameMap());
    }

    public Parent getGameViewRoot() {
        return this.gameView.getRoot();
    }

    public void startNewGame() {
        game.startNewGame();
        bindViewActions();
        refreshView();
    }

    private void bindViewActions() {
        gameView.setOnTileClicked(this::handleTileClicked);

        gameView.setTileInteractivePredicate(coords -> {
            if (!(game.getGameMap().getTile(coords) instanceof BaseTile baseTile)) {
                return false;
            }
            return baseTile.getOwner() == game.getCurrentPlayer();
        });

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
    }
}
