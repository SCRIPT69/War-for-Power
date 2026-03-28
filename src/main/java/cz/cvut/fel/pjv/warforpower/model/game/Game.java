package cz.cvut.fel.pjv.warforpower.model.game;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.players.PlayersFactory;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;

public class Game {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;
    public static final int START_MONEY_AMOUNT = 100;

    private final int playersNumber;
    private final Player[] players;
    private int currentPlayerIndex;
    private Player currentPlayer;
    private final GameMap gameMap;

    private int currentRound = 0;

    public int getPlayersNumber() {
        return playersNumber;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public Game(int playersNumber) {
        if (playersNumber < MIN_PLAYERS || playersNumber > MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    "Players number must be between " + MIN_PLAYERS + " and " + MAX_PLAYERS + "."
            );
        }
        this.playersNumber = playersNumber;
        this.players = PlayersFactory.createPlayers(playersNumber);
        this.gameMap = new GameMap();
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void startNewGame() {
        currentRound = 1;
        currentPlayerIndex = 0;
        currentPlayer = players[currentPlayerIndex];

        gameMap.generateMap(players);

        //endTurn()
        //if smth endRound()
    }

    public Unit buyUnit(UnitType unitType, HexTileCoords baseTileCoords) {
        if (unitType == null) {
            throw new IllegalArgumentException("Unit type cannot be null.");
        }
        if (baseTileCoords == null) {
            throw new IllegalArgumentException("Base tile coords cannot be null.");
        }

        BaseTile baseTile;
        if (gameMap.getTile(baseTileCoords) instanceof BaseTile base) {
            baseTile = base;
        }
        else {
            throw new IllegalArgumentException("The tile with given coordinates is not a base.");
        }
        Player owner = baseTile.getOwner();

        if (owner != currentPlayer) {
            throw new IllegalStateException("Unit can only be bought on the current player's base.");
        }
        if (owner.getMoney() < unitType.getPrice()) {
            throw new IllegalStateException("Not enough money for buying the unit.");
        }
        if (baseTile.isUnitBoughtThisRound()) {
            throw new IllegalStateException("A unit has already been bought on this base this round.");
        }
        if (baseTile.isFull()) {
            throw new IllegalStateException("The base is already full for creating a new unit.");
        }
        if (gameMap.countUnitsOfPlayer(owner) + 1 > owner.getUnitsLimit()) {
            throw new IllegalStateException("The player's unit limit would be exceeded.");
        }

        Unit newUnit = new Unit(unitType, owner, baseTile);
        baseTile.addUnit(newUnit);
        baseTile.markUnitBoughtThisRound();
        owner.decreaseMoney(unitType.getPrice());
        return newUnit;
    }

    public void endTurn() {
        currentPlayerIndex++;
        if (currentPlayerIndex >= players.length) {
            endRound();
            return;
        }
        currentPlayer = players[currentPlayerIndex];
    }
    private void endRound() {
        for (BaseTile base : gameMap.getAllBases()) {
            base.resetRoundPurchaseState();
        }

        currentPlayerIndex = 0;
        currentRound++;
        currentPlayer = players[currentPlayerIndex];
    }
}
