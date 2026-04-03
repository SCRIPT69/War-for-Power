package cz.cvut.fel.pjv.warforpower.model.game;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.players.PlayersFactory;
import cz.cvut.fel.pjv.warforpower.model.tiles.*;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;

import java.util.ArrayList;
import java.util.List;

public class Game {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;
    public static final int START_MONEY_AMOUNT = 100;
    public static final int PRICE_FOR_TILE = 50;

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
        gameMap.generateMap(players);
        currentPlayer = players[currentPlayerIndex];

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

        if (!(gameMap.getTile(baseTileCoords) instanceof BaseTile baseTile)) {
            throw new IllegalArgumentException("The tile with given coordinates is not a base.");
        }
        Player owner = baseTile.getOwner();

        if (currentPlayer.isEliminated()) {
            throw new IllegalStateException("Eliminated player cannot buy units.");
        }
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

    public List<OccupiableTile> getMovementOptions(Unit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }
        if (unit.getOwner().isEliminated()) {
            throw new IllegalStateException("Eliminated player cannot query movement options.");
        }
        if (unit.getOwner() != currentPlayer) {
            throw new IllegalStateException("Only current player's units can be queried for movement.");
        }
        OccupiableTile currentTile = unit.getOccupiedTile();

        List<OccupiableTile> currentTileNeighbours = gameMap.getNeighbourOccupiableTiles(currentTile.getTileCoords());
        List<OccupiableTile> movementOptions = new ArrayList<>();
        for (OccupiableTile tile : currentTileNeighbours) {
            if (isTileAvailableForMovement(unit, tile)) {
                movementOptions.add(tile);
            }
        }
        return movementOptions;
    }
    private boolean isTileAvailableForMovement(Unit unit, OccupiableTile tile) {
        if (!tile.hasUnits()) {
            return true;
        }

        return tile.getStandingUnits().getFirst().getOwner() == unit.getOwner() && !tile.isFull();
    }

    public void moveUnitToTile(Unit unit, OccupiableTile targetTile) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }
        if (targetTile == null) {
            throw new IllegalArgumentException("Target tile cannot be null.");
        }
        if (unit.getOwner().isEliminated()) {
            throw new IllegalStateException("Eliminated player cannot move units.");
        }
        if (unit.getOwner() != currentPlayer) {
            throw new IllegalStateException("Only current player's units can be moved.");
        }
        if (!getMovementOptions(unit).contains(targetTile)) {
            throw new IllegalStateException("Invalid tile for movement.");
        }
        OccupiableTile oldTile = unit.getOccupiedTile();
        oldTile.removeUnit(unit);
        targetTile.addUnit(unit);
        unit.setOccupiedTile(targetTile);
    }

    public void captureTile(Unit unit, OccupiableTile tile) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }
        if (tile == null) {
            throw new IllegalArgumentException("Tile for capturing cannot be null.");
        }
        if (!(tile instanceof Ownable ownableTile)) {
            throw new IllegalArgumentException("This tile cannot be captured.");
        }
        if (unit.getOccupiedTile() != tile) {
            throw new IllegalArgumentException("Unit must stand on the tile for capturing it.");
        }

        Player owner = unit.getOwner();
        if (owner.isEliminated()) {
            throw new IllegalStateException("Eliminated player cannot capture tiles.");
        }
        if (owner != currentPlayer) {
            throw new IllegalStateException("Only current player's units can capture tiles.");
        }
        if (owner == ownableTile.getOwner()) {
            throw new IllegalStateException("The tile already belongs to the player.");
        }
        if (owner.getMoney() < PRICE_FOR_TILE) {
            throw new IllegalStateException("Not enough money for capturing the tile.");
        }

        owner.decreaseMoney(PRICE_FOR_TILE);
        if (ownableTile instanceof BaseTile baseTile) {
            Player previousOwner = baseTile.getOwner();
            previousOwner.decreaseBasesCount();
            owner.increaseBasesCount();
        }
        ownableTile.setOwner(owner);
    }

    public void endTurn() {
        updateEliminatedPlayers();
        int checkedPlayers = 0;
        while (checkedPlayers < players.length) {
            currentPlayerIndex++;

            if (currentPlayerIndex >= players.length) {
                endRound();
            }

            if (!players[currentPlayerIndex].isEliminated()) {
                currentPlayer = players[currentPlayerIndex];
                return;
            }
            checkedPlayers++;
        }
        throw new IllegalStateException("No active players remaining.");
    }
    private void updateEliminatedPlayers() {
        //Eliminating players without any bases and units
        for (Player player : players) {
            if (!player.isEliminated() && player.getBasesCount() == 0 && gameMap.countUnitsOfPlayer(player) == 0) {
                player.setEliminated(true);
            }
        }
    }
    private void endRound() {
        for (BaseTile base : gameMap.getAllBases()) {
            base.resetRoundPurchaseState();
        }

        currentPlayerIndex = 0;
        currentRound++;
    }
}
