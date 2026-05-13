package cz.cvut.fel.pjv.warforpower.model.game;

import cz.cvut.fel.pjv.warforpower.model.battle.BattleAttemptResult;
import cz.cvut.fel.pjv.warforpower.model.battle.BattleOutcome;
import cz.cvut.fel.pjv.warforpower.model.battle.BattleResolver;
import cz.cvut.fel.pjv.warforpower.model.battle.BattleResult;
import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.players.PlayerColor;
import cz.cvut.fel.pjv.warforpower.model.players.PlayersFactory;
import cz.cvut.fel.pjv.warforpower.model.score.GameScoreResult;
import cz.cvut.fel.pjv.warforpower.model.score.ScoreCalculator;
import cz.cvut.fel.pjv.warforpower.model.score.ScoreResult;
import cz.cvut.fel.pjv.warforpower.model.tiles.*;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;

import java.util.ArrayList;
import java.util.List;

import cz.cvut.fel.pjv.warforpower.save.GameSnapshot;
import cz.cvut.fel.pjv.warforpower.save.PlayerSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);

    private final int playersNumber;
    private final Player[] players;
    private int currentPlayerIndex;
    private Player currentPlayer;
    private final GameMap gameMap;
    private final ScoreCalculator scoreCalculator;
    private final UnitActionTilesResolver unitActionTilesResolver;
    private final BattleResolver battleResolver;

    private boolean gameEnded = false;
    private GameScoreResult finalScoreResult;

    private int currentRound = 0;

    public int getPlayersNumber() {
        return playersNumber;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }

    public GameScoreResult getFinalScoreResult() {
        if (!gameEnded || finalScoreResult == null) {
            throw new IllegalStateException("Game has not ended yet.");
        }

        return finalScoreResult;
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
     * Returns all players participating in the game.
     *
     * @return players array
     */
    public Player[] getPlayers() {
        return players;
    }

    /**
     * Returns index of the current active player.
     *
     * @return current player index
     */
    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
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
        this.unitActionTilesResolver = new UnitActionTilesResolver(this);
        this.battleResolver = new BattleResolver();
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
     * Restores complete game state from a saved snapshot.
     *
     * This method is used only when loading a saved game. It restores players,
     * map state, units, current round and current active player.
     *
     * @param snapshot saved game snapshot
     */
    public void restoreFromSnapshot(GameSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Game snapshot cannot be null.");
        }
        if (snapshot.players().size() != players.length) {
            throw new IllegalStateException("Saved players count does not match game players count.");
        }

        for (int i = 0; i < players.length; i++) {
            PlayerSnapshot playerSnapshot = snapshot.players().get(i);

            players[i] = new Player(
                    playerSnapshot.name(),
                    PlayerColor.valueOf(playerSnapshot.color()),
                    playerSnapshot.money()
            );

            for (int baseIndex = 0; baseIndex < playerSnapshot.basesCount(); baseIndex++) {
                players[i].increaseBasesCount();
            }

            if (playerSnapshot.eliminated()) {
                players[i].setEliminated(true);
            }
        }

        gameMap.restoreFromSnapshot(snapshot.tiles(), snapshot.units(), players);

        currentRound = snapshot.currentRound();
        currentPlayerIndex = snapshot.currentPlayerIndex();

        if (currentPlayerIndex < 0 || currentPlayerIndex >= players.length) {
            throw new IllegalStateException("Saved current player index is invalid.");
        }

        currentPlayer = players[currentPlayerIndex];

        LOGGER.info("Game state restored from snapshot. Round: {}, Player: {}.",
                currentRound,
                currentPlayer.getDisplayLabel());
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

        LOGGER.info("New game started with {} players.", playersNumber);
        LOGGER.info("First active player: {}.", currentPlayer.getDisplayLabel());
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
        if (!baseTile.canBuyUnitThisRound()) {
            throw new IllegalStateException("A unit cannot be bought on this base this round.");
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

        LOGGER.info("Player {} bought {} on base {}.",
                owner.getDisplayLabel(), unitType, baseTileCoords);

        return newUnit;
    }

    /**
     * Returns all neighbouring occupiable tiles that the given unit
     * can move to during the current turn.
     *
     * @param unit unit whose movement options are requested
     * @return list of valid movement target tiles
     */
    public List<OccupiableTile> getMovementOptions(Unit unit) {
        return unitActionTilesResolver.getMovementOptions(unit);
    }

    /**
     * Returns all neighbouring occupiable tiles that the given unit
     * can attack during the current turn.
     *
     * @param unit unit whose attack options are requested
     * @return list of valid attack target tiles
     */
    public List<HexTile> getAttackOptions(Unit unit) {
        return unitActionTilesResolver.getAttackOptions(unit);
    }

    /**
     * Returns movement tiles that two units may enter together.
     *
     * @param first first unit
     * @param second second unit
     * @return shared movement target tiles
     */
    public List<OccupiableTile> getSharedMovementOptions(Unit first, Unit second) {
        return unitActionTilesResolver.getSharedMovementOptions(first, second);
    }

    /**
     * Returns attack tiles that two units may attack together.
     *
     * @param first first unit
     * @param second second unit
     * @return shared attack target tiles
     */
    public List<HexTile> getSharedAttackOptions(Unit first, Unit second) {
        return unitActionTilesResolver.getSharedAttackOptions(first, second);
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

        LOGGER.info("Player {} moved {} from {} to {}.",
                unit.getOwner().getDisplayLabel(),
                unit.getUnitType(),
                oldTile.getTileCoords(),
                targetTile.getTileCoords());
    }
    /**
     * Moves two units to the same target tile if the shared move is valid.
     *
     * @param first first unit to move
     * @param second second unit to move
     * @param targetTile destination tile
     * @throws IllegalArgumentException if arguments are invalid
     * @throws IllegalStateException if the move is not allowed
     */
    public void moveUnitsToTile(Unit first, Unit second, OccupiableTile targetTile) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Units cannot be null.");
        }
        if (targetTile == null) {
            throw new IllegalArgumentException("Target tile cannot be null.");
        }
        if (first == second) {
            throw new IllegalArgumentException("Two different units are required for shared movement.");
        }

        if (first.getOwner() != second.getOwner()) {
            throw new IllegalStateException("Both units must belong to the same player.");
        }
        Player owner = first.getOwner();
        if (owner.isEliminated()) {
            throw new IllegalStateException("Eliminated player cannot move units.");
        }
        if (owner != currentPlayer) {
            throw new IllegalStateException("Only current player's units can be moved.");
        }

        if (first.hasUsedMainActionThisRound() || second.hasUsedMainActionThisRound()) {
            throw new IllegalStateException("Both units must still have their main action available.");
        }
        if (!getSharedMovementOptions(first, second).contains(targetTile)) {
            throw new IllegalStateException("Invalid tile for shared movement.");
        }

        OccupiableTile firstOldTile = first.getOccupiedTile();
        OccupiableTile secondOldTile = second.getOccupiedTile();

        firstOldTile.removeUnit(first);
        secondOldTile.removeUnit(second);

        targetTile.addUnit(first);
        targetTile.addUnit(second);

        first.setOccupiedTile(targetTile);
        second.setOccupiedTile(targetTile);

        first.markUsedMainActionThisRound();
        second.markUsedMainActionThisRound();

        if (targetTile instanceof BaseTile baseTile && baseTile.getOwner() != first.getOwner()) {
            captureBase(first.getOwner(), baseTile);
        }

        LOGGER.info("Player {} moved {} from {} and {} from {} to {}.",
                first.getOwner().getDisplayLabel(),
                first.getUnitType(),
                firstOldTile.getTileCoords(),
                second.getUnitType(),
                secondOldTile.getTileCoords(),
                targetTile.getTileCoords());
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

        assignBaseToOwner(newOwner, baseTile);

        LOGGER.info("Player {} captured enemy base at {}.",
                newOwner.getDisplayLabel(), baseTile.getTileCoords());
    }

    /**
     * Assigns base ownership to the specified player and marks the base
     * as unavailable for recruitment in the current round.
     *
     * @param newOwner new base owner
     * @param baseTile base tile
     */
    private void assignBaseToOwner(Player newOwner, BaseTile baseTile) {
        if (newOwner == null) {
            throw new IllegalArgumentException("New owner cannot be null.");
        }
        if (baseTile == null) {
            throw new IllegalArgumentException("Base tile cannot be null.");
        }

        baseTile.setOwner(newOwner);
        // A base captured during this round cannot be used for buying units
        baseTile.markCapturedThisRound();
        newOwner.increaseBasesCount();
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

        LOGGER.info("Player {} captured tile {}.",
                owner.getDisplayLabel(), tile.getTileCoords());
    }

    /**
     * Resolves battle for the specified attacking units and target tile.
     * Attacking units consume their main action only after the battle
     * is successfully resolved.
     *
     * @param attackingUnits attacking units
     * @param targetCoords coordinates of the attacked tile
     * @return resolved battle result
     */
    public BattleResult resolveBattle(List<Unit> attackingUnits, HexTileCoords targetCoords) {
        if (attackingUnits == null || attackingUnits.isEmpty()) {
            throw new IllegalArgumentException("Attacking units cannot be null or empty.");
        }
        if (targetCoords == null) {
            throw new IllegalArgumentException("Target coordinates cannot be null.");
        }

        validateAttackingUnitsForBattle(attackingUnits);

        HexTile targetTile = gameMap.getTile(targetCoords);

        if (attackingUnits.size() == 1) {
            Unit attackingUnit = attackingUnits.getFirst();

            if (!getAttackOptions(attackingUnit).contains(targetTile)) {
                throw new IllegalStateException("Invalid target tile for attack.");
            }
        } else if (attackingUnits.size() == 2) {
            Unit firstUnit = attackingUnits.getFirst();
            Unit secondUnit = attackingUnits.get(1);

            if (!getSharedAttackOptions(firstUnit, secondUnit).contains(targetTile)) {
                throw new IllegalStateException("Invalid target tile for shared attack.");
            }
        } else {
            throw new IllegalStateException("Only 1 or 2 attacking units are supported.");
        }

        BattleResult battleResult;

        if (targetTile instanceof OccupiableTile occupiableTile) {
            if (!occupiableTile.hasUnits()) {
                throw new IllegalStateException("Target occupiable tile has no defending units.");
            }

            battleResult = battleResolver.resolvePlayerVsPlayer(
                    attackingUnits,
                    occupiableTile.getStandingUnits(),
                    occupiableTile
            );
        } else if (targetTile instanceof CityTile cityTile) {
            battleResult = battleResolver.resolvePlayerVsCity(attackingUnits, cityTile);
        } else {
            throw new IllegalStateException("Unsupported battle target tile.");
        }

        for (Unit attackingUnit : attackingUnits) {
            attackingUnit.markUsedMainActionThisRound();
        }

        return battleResult;
    }
    /**
     * Validates attacking units before battle resolution.
     *
     * @param attackingUnits attacking units
     */
    private void validateAttackingUnitsForBattle(List<Unit> attackingUnits) {
        Player owner = attackingUnits.getFirst().getOwner();

        for (Unit attackingUnit : attackingUnits) {
            if (attackingUnit == null) {
                throw new IllegalArgumentException("Attacking unit cannot be null.");
            }
            if (attackingUnit.getOwner() != owner) {
                throw new IllegalStateException("All attacking units must belong to the same player.");
            }
            if (attackingUnit.getOwner().isEliminated()) {
                throw new IllegalStateException("Eliminated player cannot attack.");
            }
            if (attackingUnit.getOwner() != currentPlayer) {
                throw new IllegalStateException("Only current player's units can attack.");
            }
            if (attackingUnit.hasUsedMainActionThisRound()) {
                throw new IllegalStateException("Attacking unit has already used its main action this round.");
            }
        }
    }

    /**
     * Applies final battle result to the game state.
     *
     * Losing units are removed from the map.
     * If attackers win, surviving attackers move to the battle tile.
     * If the battle was for a city and attackers win, the city is converted to a base.
     *
     * @param battleResult resolved battle result
     */
    public void applyBattleResult(BattleResult battleResult) {
        if (battleResult == null) {
            throw new IllegalArgumentException("Battle result cannot be null.");
        }

        BattleAttemptResult finalAttempt = battleResult.hasSecondAttempt()
                ? battleResult.secondAttempt()
                : battleResult.firstAttempt();

        removeUnits(finalAttempt.attackerResult().lostUnits());
        removeUnits(finalAttempt.defenderResult().lostUnits());

        if (battleResult.finalOutcome() == BattleOutcome.DRAW) {
            LOGGER.info("Battle at {} ended in a draw.", battleResult.tileOfBattle().getTileCoords());
            return;
        }

        if (battleResult.finalOutcome() == BattleOutcome.DEFENDER_WIN) {
            LOGGER.info("Defenders won the battle at {}.", battleResult.tileOfBattle().getTileCoords());
            return;
        }

        List<Unit> survivingAttackers = getSurvivingUnits(
                finalAttempt.attackerResult().units(),
                finalAttempt.attackerResult().lostUnits()
        );

        if (survivingAttackers.isEmpty()) {
            throw new IllegalStateException("Attackers won, but no surviving attackers remain.");
        }

        Player newOwner = survivingAttackers.getFirst().getOwner();

        OccupiableTile destinationTile;
        if (battleResult.tileOfBattle() instanceof CityTile cityTile) {
            destinationTile = convertCityToBase(cityTile, newOwner);
        } else if (battleResult.tileOfBattle() instanceof OccupiableTile occupiableTile) {
            destinationTile = occupiableTile;
        } else {
            throw new IllegalStateException("Unsupported battle destination tile.");
        }

        moveUnitsToBattleTile(survivingAttackers, destinationTile);

        if (destinationTile instanceof BaseTile baseTile && baseTile.getOwner() != newOwner) {
            captureBase(newOwner, baseTile);
        }

        LOGGER.info("Attackers won the battle at {}.", battleResult.tileOfBattle().getTileCoords());
    }
    /**
     * Removes units from their current occupied tiles.
     *
     * @param units units to remove
     */
    private void removeUnits(List<Unit> units) {
        for (Unit unit : units) {
            if (unit == null) {
                throw new IllegalStateException("Battle result contains null unit.");
            }

            OccupiableTile occupiedTile = unit.getOccupiedTile();
            if (occupiedTile == null) {
                throw new IllegalStateException("Unit to remove is not placed on any tile.");
            }
            occupiedTile.removeUnit(unit);
        }
    }
    /**
     * Returns units that survived the battle.
     *
     * @param units all participating units
     * @param lostUnits units lost in the battle
     * @return surviving units
     */
    private List<Unit> getSurvivingUnits(List<Unit> units, List<Unit> lostUnits) {
        List<Unit> survivingUnits = new ArrayList<>();

        for (Unit unit : units) {
            if (!lostUnits.contains(unit)) {
                survivingUnits.add(unit);
            }
        }

        return survivingUnits;
    }
    /**
     * Moves surviving attacking units from their original tiles
     * to the destination battle tile.
     *
     * @param units surviving attacking units
     * @param destinationTile destination tile
     */
    private void moveUnitsToBattleTile(List<Unit> units, OccupiableTile destinationTile) {
        for (Unit unit : units) {
            OccupiableTile oldTile = unit.getOccupiedTile();
            if (oldTile != null) {
                oldTile.removeUnit(unit);
            }

            destinationTile.addUnit(unit);
            unit.setOccupiedTile(destinationTile);
        }
    }

    /**
     * Converts the specified city tile into a new base owned by the given player.
     * Newly captured base cannot recruit units in the current round.
     *
     * @param cityTile city tile to convert
     * @param newOwner owner of the new base
     * @return newly created base tile
     */
    private BaseTile convertCityToBase(CityTile cityTile, Player newOwner) {
        if (cityTile == null) {
            throw new IllegalArgumentException("City tile cannot be null.");
        }
        if (newOwner == null) {
            throw new IllegalArgumentException("New owner cannot be null.");
        }

        BaseTile newBase = new BaseTile(cityTile.getTileCoords(), newOwner);
        assignBaseToOwner(newOwner, newBase);

        gameMap.replaceTile(cityTile.getTileCoords(), newBase);

        LOGGER.info("City at {} was converted into a base of player {}.",
                cityTile.getTileCoords(),
                newOwner.getDisplayLabel());

        return newBase;
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
                boolean gameEnded = endRound();
                if (gameEnded) {
                    return;
                }
            }

            if (!players[currentPlayerIndex].isEliminated()) {
                currentPlayer = players[currentPlayerIndex];
                LOGGER.info("Current player is now {}.", currentPlayer.getDisplayLabel());
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
                LOGGER.info("Player {} was eliminated.", player.getDisplayLabel());
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
     *
     * @return true if the game ended instead of starting a new round
     */
    private boolean endRound() {
        if (currentRound == NUMBER_OF_ROUNDS) {
            endGame();
            return true;
        }
        LOGGER.info("Round {} ended.", currentRound);

        for (BaseTile base : gameMap.getAllBases()) {
            base.resetRoundState();
        }
        resetUnitsRoundActionState();
        increasePlayersMoneyForBases();

        currentPlayerIndex = 0;
        currentRound++;
        LOGGER.info("Round {} started.", currentRound);

        return false;
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
     */
    public void endGame() {
        if (gameEnded) {
            return;
        }

        gameEnded = true;
        finalScoreResult = scoreCalculator.calculateGameResult(gameMap, players);

        LOGGER.info("Game ended.");

        for (ScoreResult result : finalScoreResult.playerScores()) {
            LOGGER.info("Player {} final score: {}.",
                    result.player().getDisplayLabel(),
                    result.getTotalPoints());
        }
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
