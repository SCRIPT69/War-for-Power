package cz.cvut.fel.pjv.warforpower.model.game;

import cz.cvut.fel.pjv.warforpower.model.tiles.CityTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolves neighbouring tiles available to units
 * for movement and attack in the current game state.
 */
public class UnitActionTilesResolver {
    private final Game game;

    public UnitActionTilesResolver(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }
        this.game = game;
    }

    /**
     * Returns all neighbouring occupiable tiles the unit may move to.
     *
     * @param unit unit whose movement options are requested
     * @return valid movement target tiles
     */
    public List<OccupiableTile> getMovementOptions(Unit unit) {
        validateUnitForActionQuery(unit);

        List<OccupiableTile> movementOptions = new ArrayList<>();
        for (OccupiableTile tile : getNeighbourOccupiableTiles(unit)) {
            if (isTileAvailableForMovement(unit, tile)) {
                movementOptions.add(tile);
            }
        }
        return movementOptions;
    }

    /**
     * Returns all neighbouring tiles the unit may attack.
     * Attack targets may include both occupiable enemy tiles and city tiles.
     *
     * @param unit unit whose attack options are requested
     * @return valid attack target tiles
     */
    public List<HexTile> getAttackOptions(Unit unit) {
        validateUnitForActionQuery(unit);

        List<HexTile> attackOptions = new ArrayList<>();
        for (HexTile tile : getNeighbourTiles(unit)) {
            if (isTileAvailableForAttack(unit, tile)) {
                attackOptions.add(tile);
            }
        }
        return attackOptions;
    }

    /**
     * Returns occupiable neighbour tiles of the unit's current tile.
     *
     * @param unit unit whose neighbours are requested
     * @return neighbouring occupiable tiles
     */
    private List<OccupiableTile> getNeighbourOccupiableTiles(Unit unit) {
        return game.getGameMap().getNeighbourOccupiableTiles(unit.getOccupiedTile().getTileCoords());
    }

    /**
     * Returns all neighbour tiles of the unit's current tile.
     *
     * @param unit unit whose neighbours are requested
     * @return neighbouring tiles
     */
    private List<HexTile> getNeighbourTiles(Unit unit) {
        return game.getGameMap().getNeighbourTiles(unit.getOccupiedTile().getTileCoords());
    }

    /**
     * Checks whether a tile may be entered by the specified unit.
     *
     * @param unit unit that wants to move
     * @param tile target tile
     * @return true if movement to the tile is allowed
     */
    private boolean isTileAvailableForMovement(Unit unit, OccupiableTile tile) {
        if (!tile.hasUnits()) {
            return true;
        }

        return tile.getStandingUnits().getFirst().getOwner() == unit.getOwner() && !tile.isFull();
    }

    /**
     * Checks whether a tile may be attacked by the specified unit.
     * A tile is attackable if it is a city tile, or if it is an occupiable tile
     * containing enemy units.
     *
     * @param unit unit that wants to attack
     * @param tile target tile
     * @return true if attack on the tile is allowed
     */
    private boolean isTileAvailableForAttack(Unit unit, HexTile tile) {
        if (tile instanceof CityTile) {
            return true;
        }

        if (tile instanceof OccupiableTile occupiableTile) {
            return occupiableTile.hasUnits()
                    && occupiableTile.getStandingUnits().getFirst().getOwner() != unit.getOwner();
        }

        return false;
    }

    /**
     * Validates that the unit may be queried for available actions
     * in the current game state.
     *
     * @param unit unit to validate
     */
    private void validateUnitForActionQuery(Unit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }
        if (unit.getOwner().isEliminated()) {
            throw new IllegalStateException("Eliminated player cannot query unit actions.");
        }
        if (unit.getOwner() != game.getCurrentPlayer()) {
            throw new IllegalStateException("Only current player's units can be queried for actions.");
        }
        if (unit.hasUsedMainActionThisRound()) {
            throw new IllegalStateException("Unit has already used its main action this round.");
        }
    }


    /**
     * Returns movement tiles that two units may enter together.
     * A shared movement tile must be reachable by both units individually
     * and must have enough free capacity for both incoming units.
     *
     * @param first first unit
     * @param second second unit
     * @return valid shared movement target tiles
     */
    public List<OccupiableTile> getSharedMovementOptions(Unit first, Unit second) {
        validateUnitsForSharedActionQuery(first, second);

        List<OccupiableTile> firstOptions = getMovementOptions(first);
        List<OccupiableTile> secondOptions = getMovementOptions(second);

        List<OccupiableTile> sharedOptions = new ArrayList<>();
        for (OccupiableTile tile : firstOptions) {
            if (secondOptions.contains(tile) && canUnitsMoveTogetherToTile(first, second, tile)) {
                sharedOptions.add(tile);
            }
        }

        return sharedOptions;
    }

    /**
     * Returns attack tiles that both units may attack together.
     *
     * @param first first unit
     * @param second second unit
     * @return valid shared attack target tiles
     */
    public List<HexTile> getSharedAttackOptions(Unit first, Unit second) {
        validateUnitsForSharedActionQuery(first, second);

        List<HexTile> firstOptions = getAttackOptions(first);
        List<HexTile> secondOptions = getAttackOptions(second);

        List<HexTile> sharedOptions = new ArrayList<>();
        for (HexTile tile : firstOptions) {
            if (secondOptions.contains(tile)) {
                sharedOptions.add(tile);
            }
        }

        return sharedOptions;
    }

    /**
     * Checks whether two units may move together to the same target tile.
     * Both units must be distinct and the resulting number of units
     * on the tile must not exceed tile capacity.
     *
     * @param first first unit
     * @param second second unit
     * @param targetTile shared movement target tile
     * @return true if both units may enter the tile together
     */
    private boolean canUnitsMoveTogetherToTile(Unit first, Unit second, OccupiableTile targetTile) {
        if (first == second) {
            return false;
        }

        int currentUnitsOnTile = targetTile.getStandingUnits().size();
        int futureUnitsOnTile = currentUnitsOnTile + 2;

        return futureUnitsOnTile <= OccupiableTile.MAX_UNITS;
    }

    /**
     * Validates that two units may be queried together for shared actions.
     *
     * @param first first unit
     * @param second second unit
     */
    private void validateUnitsForSharedActionQuery(Unit first, Unit second) {
        validateUnitForActionQuery(first);
        validateUnitForActionQuery(second);

        if (first == second) {
            throw new IllegalArgumentException("Shared action query requires two different units.");
        }
    }
}
