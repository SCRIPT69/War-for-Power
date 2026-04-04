package cz.cvut.fel.pjv.warforpower.model.tiles;

import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract tile type that can contain units standing on it.
 */
public abstract class OccupiableTile extends HexTile {
    private static final int MAX_UNITS = 2;
    private final List<Unit> standingUnits = new ArrayList<>(MAX_UNITS);

    public OccupiableTile(HexTileCoords tileCoords, HexTileType tileType) {
        super(tileCoords, tileType);
    }

    /**
     * Returns all units currently standing on this tile.
     *
     * @return list of standing units
     */
    public List<Unit> getStandingUnits() {
        return Collections.unmodifiableList(standingUnits);
    }

    /**
     * Adds a unit to this tile.
     *
     * @param unit unit to add
     */
    public void addUnit(Unit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }
        if (isFull()) {
            throw new IllegalStateException("Tile already contains maximum number of units.");
        }
        standingUnits.add(unit);
    }

    /**
     * Removes a unit from this tile.
     *
     * @param unit unit to remove
     */
    public void removeUnit(Unit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }
        if (!tryRemoveUnit(unit)) {
            throw new IllegalStateException("Unit is not standing on this tile.");
        }
    }
    private boolean tryRemoveUnit(Unit unit) {
        return standingUnits.remove(unit);
    }

    /**
     * Returns whether the tile has reached its unit capacity.
     *
     * @return true if no more units may be placed on this tile
     */
    public boolean isFull() {
        return standingUnits.size() >= MAX_UNITS;
    }

    /**
     * Returns whether the tile currently contains any units.
     *
     * @return true if at least one unit stands on the tile
     */
    public boolean hasUnits() {
        return !standingUnits.isEmpty();
    }

    public int getStandingUnitsCount() {
        return standingUnits.size();
    }
}
