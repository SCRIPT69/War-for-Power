package cz.cvut.fel.pjv.warforpower.model.tiles;

import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class OccupiableTile extends HexTile {
    private static final int MAX_UNITS = 2;
    private final List<Unit> standingUnits = new ArrayList<>(MAX_UNITS);

    public OccupiableTile(HexTileCoords tileCoords, HexTileType tileType) {
        super(tileCoords, tileType);
    }

    public List<Unit> getStandingUnits() {
        return Collections.unmodifiableList(standingUnits);
    }

    public void addUnit(Unit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }
        if (isFull()) {
            throw new IllegalStateException("Tile already contains maximum number of units.");
        }
        standingUnits.add(unit);
    }

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

    public boolean isFull() {
        return standingUnits.size() >= MAX_UNITS;
    }

    public boolean hasUnits() {
        return !standingUnits.isEmpty();
    }

    public int getStandingUnitsCount() {
        return standingUnits.size();
    }
}
