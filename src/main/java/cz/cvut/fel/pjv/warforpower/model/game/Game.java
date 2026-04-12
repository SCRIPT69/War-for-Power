package cz.cvut.fel.pjv.warforpower.model.game;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.players.PlayersFactory;
import cz.cvut.fel.pjv.warforpower.model.score.GameScoreResult;
import cz.cvut.fel.pjv.warforpower.model.score.ScoreCalculator;
import cz.cvut.fel.pjv.warforpower.model.tiles.*;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Central class coordinating the main game rules, round progression
 * and turn order of all players.
 */
public class Game {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;
    public static final int NUMBER_OF_ROUNDS = 15;
    public static final int START_MONEY_AMOUNT = 100;
    public static final int PRICE_FOR_TILE = 50;
    public static final int MONEY_PER_BASE = 50;

    private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

    private final int playersNumber;
    private final Player[] players;
    private int currentPlayerIndex;
    private Player currentPlayer;
    private final GameMap gameMap;
    private final ScoreCalculator scoreCalculator;

    private int currentRound = 0;

    public int getPlayersNumber() {
        return playersNumber;
    }

    /**
     * Returns the player whose turn is currently active.
     *
     * @return current player
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    /**
     * Creates a new game instance for the given number of players.
     *
     * @param playersNumber number of players
     * @throws IllegalArgumentException if the number of players is outside the allowed range
     */
    public Game(int playersNumber) {
        if (playersNumber < MIN_PLAYERS || playersNumber > MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    "Players number must be between " + MIN_PLAYERS + " and " + MAX_PLAYERS + "."
            );
        }
        this.playersNumber = playersNumber;
        this.players = PlayersFactory.createPlayers(playersNumber);
        this.gameMap = new GameMap();
        this.scoreCalculator = new ScoreCalculator();
    }

    /**
     * Returns the game map associated with this game.
     *
     * @return game map
     */
    public GameMap getGameMap() {
        return gameMap;
    }

    /**
     * Initializes a new game round sequence, creates the map
     * and sets the first active player.
     */
    public void startNewGame() {
        currentRound = 1;
        currentPlayerIndex = 0;
        gameMap.generateMap(players);
        currentPlayer = players[currentPlayerIndex];

        LOGGER.info("New game started with " + playersNumber + " players.");
        LOGGER.info("First active player: " + currentPlayer.getDisplayLabel() + ".");
    }

    /**
     * Buys a new unit on the specified base tile if all game rules are satisfied.
     *
     * @param unitType type of unit to buy
     * @param baseTileCoords coordinates of the base tile
     * @return newly created unit
     * @throws IllegalArgumentException if arguments are invalid
     * @throws IllegalStateException if the purchase is not allowed by current game rules
     */
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
        newUnit.markUsedMainActionThisRound(); // newly recruited units cannot act in the same round
        baseTile.markUnitBoughtThisRound(); // only one unit can be recruited per base in a round
        owner.decreaseMoney(unitType.getPrice());

        LOGGER.info("Player " + owner.getDisplayLabel()
                + " bought " + unitType
                + " on base " + baseTileCoords + ".");

        return newUnit;
    }

    /**
     * Returns all neighbouring occupiable tiles that the given unit
     * can move to during the current turn.
     *
     * @param unit unit whose movement options are requested
     * @return list of valid movement target tiles
     * @throws IllegalArgumentException if the unit is null
     * @throws IllegalStateException if the unit is not allowed to act
     */
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
        if (unit.hasUsedMainActionThisRound()) {
            throw new IllegalStateException("Unit has already used its main action this round.");
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
    /**
     * Checks whether a tile can be entered by the given unit.
     * A tile is available if it is empty, or if it contains only allied units
     * and still has free capacity.
     *
     * @param unit unit that wants to move
     * @param tile target tile
     * @return true if the tile is available for movement
     */
    private boolean isTileAvailableForMovement(Unit unit, OccupiableTile tile) {
        if (!tile.hasUnits()) {
            return true;
        }

        return tile.getStandingUnits().getFirst().getOwner() == unit.getOwner() && !tile.isFull();
    }

    /**
     * Moves the given unit to the target tile if the move is valid.
     *
     * @param unit unit to move
     * @param targetTile destination tile
     * @throws IllegalArgumentException if arguments are invalid
     * @throws IllegalStateException if the move is not allowed
     */
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
        if (unit.hasUsedMainActionThisRound()) {
            throw new IllegalStateException("Unit has already used its main action this round.");
        }
        if (!getMovementOptions(unit).contains(targetTile)) {
            throw new IllegalStateException("Invalid tile for movement.");
        }
        OccupiableTile oldTile = unit.getOccupiedTile();
        oldTile.removeUnit(unit);
        targetTile.addUnit(unit);
        unit.markUsedMainActionThisRound();
        unit.setOccupiedTile(targetTile);

        if (targetTile instanceof BaseTile baseTile && baseTile.getOwner() != unit.getOwner()) {
            captureBase(unit.getOwner(), baseTile);
        }

        LOGGER.info("Player " + unit.getOwner().getDisplayLabel()
                + " moved " + unit.getUnitType()
                + " from " + oldTile.getTileCoords()
                + " to " + targetTile.getTileCoords() + ".");
    }

    /**
     * Transfers ownership of a base to the specified player.
     *
     * @param newOwner player who captures the base
     * @param baseTile captured base tile
     * @throws IllegalArgumentException if arguments are invalid
     * @throws IllegalStateException if the base already belongs to the player
     */
    private void captureBase(Player newOwner, BaseTile baseTile) {
        if (newOwner == null) {
            throw new IllegalArgumentException("New owner cannot be null.");
        }
        if (baseTile == null) {
            throw new IllegalArgumentException("Base tile cannot be null.");
        }
        if (baseTile.getOwner() == newOwner) {
            throw new IllegalStateException("The base already belongs to the player.");
        }

        Player previousOwner = baseTile.getOwner();
        previousOwner.decreaseBasesCount();
        newOwner.increaseBasesCount();
        baseTile.setOwner(newOwner);
        baseTile.markUnitBoughtThisRound(); // player can not buy units on the new base in the current round

        LOGGER.info("Player " + newOwner.getDisplayLabel()
                + " captured enemy base at " + baseTile.getTileCoords() + ".");
    }

    /**
     * Captures the specified terrain tile for the current player's unit.
     * Capturing a terrain tile does not consume the unit's main move/attack action for the round.
     *
     * @param unit unit performing the capture
     * @param tile terrain tile to capture
     * @throws IllegalArgumentException if arguments are invalid
     * @throws IllegalStateException if the capture is not allowed
     */
    public void captureTerrainTile(Unit unit, TerrainTile tile) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }
        if (tile == null) {
            throw new IllegalArgumentException("Terrain tile cannot be null.");
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
        if (owner == tile.getOwner()) {
            throw new IllegalStateException("The tile already belongs to the player.");
        }
        if (owner.getMoney() < PRICE_FOR_TILE) {
            throw new IllegalStateException("Not enough money for capturing the tile.");
        }

        owner.decreaseMoney(PRICE_FOR_TILE);
        tile.setOwner(owner);

        LOGGER.info("Player " + owner.getDisplayLabel()
                + " captured tile " + tile.getTileCoords() + ".");
    }

    /**
     * Ends the current player's turn and advances the game
     * to the next non-eliminated player.
     *
     * @throws IllegalStateException if no active players remain in the game
     */
    public void endTurn() {
        // Search for the next non-eliminated player. If the index exceeds the array,
        // a new round starts and the search continues from the first player.

        updateEliminatedPlayers();
        int activePlayersCount = getActivePlayersCount();
        if (activePlayersCount == 1) {
            endGame();
            return;
        }
        else if (activePlayersCount == 0) {
            throw new IllegalStateException("No active players remaining.");
        }

        int checkedPlayers = 0;
        while (checkedPlayers < players.length) {
            currentPlayerIndex++;

            if (currentPlayerIndex >= players.length) {
                endRound();
            }

            if (!players[currentPlayerIndex].isEliminated()) {
                currentPlayer = players[currentPlayerIndex];
                LOGGER.info("Current player is now " + currentPlayer.getDisplayLabel() + ".");
                return;
            }
            checkedPlayers++;
        }
        throw new IllegalStateException("No active players remaining.");
    }
    /**
     * Updates elimination status of players who no longer own any bases
     * and do not control any units on the map.
     */
    private void updateEliminatedPlayers() {
        // A player is eliminated once they control no bases and no units.
        for (Player player : players) {
            if (!player.isEliminated() && player.getBasesCount() == 0 && gameMap.countUnitsOfPlayer(player) == 0) {
                player.setEliminated(true);
                LOGGER.info("Player " + player.getDisplayLabel() + " was eliminated.");
            }
        }
    }
    /**
     * Counts players who are still active in the game.
     *
     * @return number of active players
     */
    private int getActivePlayersCount() {
        int activePlayersCount = 0;
        for (Player player : players) {
            if (!player.isEliminated()) {
                activePlayersCount++;
            }
        }
        return activePlayersCount;
    }
    /**
     * Finishes the current round, resets round-based temporary states, unit round-action state
     * and awards base income to all active players.
     */
    private void endRound() {
        if (currentRound == NUMBER_OF_ROUNDS) {
            endGame();
            return;
        }
        LOGGER.info("Round " + currentRound + " ended.");

        for (BaseTile base : gameMap.getAllBases()) {
            base.resetRoundPurchaseState();
        }
        resetUnitsRoundActionState();
        increasePlayersMoneyForBases();

        currentPlayerIndex = 0;
        currentRound++;
        LOGGER.info("Round " + currentRound + " started.");
    }
    /**
     * Resets round-based main action state of all units currently present on the map.
     */
    private void resetUnitsRoundActionState() {
        for (Unit unit : gameMap.getAllUnits()) {
            unit.resetRoundActionState();
        }
    }
    /**
     * Awards round income to players according to the number of bases they control.
     */
    private void increasePlayersMoneyForBases() {
        for (Player player : players) {
            if (player.isEliminated()) {
                continue;
            }

            int basesCount = player.getBasesCount();
            if (basesCount > 0) {
                player.increaseMoney(basesCount * MONEY_PER_BASE);
            }
        }
    }

    /**
     * Ends the game and prepares final score evaluation and end-game handling.
     * The full end-game flow is intended to be completed in later implementation stages.
     */
    public void endGame() {
        //GameScoreResult gameScoreResult = calculateFinalScore();
        //and other logic
        LOGGER.info("Game ended.");
    }

    /**
     * Calculates final score results of all players.
     *
     * @return final game score result
     */
    public GameScoreResult calculateFinalScore() {
        return scoreCalculator.calculateGameResult(gameMap, players);
    }
}
