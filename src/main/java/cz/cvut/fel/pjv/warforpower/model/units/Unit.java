package cz.cvut.fel.pjv.warforpower.model.units;

import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainType;

/**
 * Represents a single unit controlled by a player.
 * Each unit has a type, owner and currently occupied tile.
 */
public class Unit {
    private final Player owner;
    private OccupiableTile occupiedTile;
    private static final int TERRAIN_ADVANTAGE_BONUS = 2;
    private static final int TERRAIN_DISADVANTAGE_PENALTY = -2;

    private final UnitType unitType;
    private boolean actedThisRound = false;

    public Unit(UnitType unitType, Player owner, OccupiableTile occupiedTile) {
        if (unitType == null) {
            throw new IllegalArgumentException("Unit type cannot be null.");
        }
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null.");
        }
        if (occupiedTile == null) {
            throw new IllegalArgumentException("Occupied tile cannot be null.");
        }

        this.unitType = unitType;
        this.owner = owner;
        this.occupiedTile = occupiedTile;
    }

    public UnitType getUnitType() {
        return unitType;
    }

    public TerrainType getAdvantageTerrainType() {
        return unitType.getAdvantageTerrainType();
    }

    public TerrainType getDisadvantageTerrainType() {
        return unitType.getDisadvantageTerrainType();
    }

    public Player getOwner() {
        return owner;
    }

    public OccupiableTile getOccupiedTile() {
        return occupiedTile;
    }

    public void setOccupiedTile(OccupiableTile occupiedTile) {
        if (occupiedTile == null) {
            throw new IllegalArgumentException("Occupied tile cannot be null.");
        }
        this.occupiedTile = occupiedTile;
    }

    public int getTerrainModifier(TerrainType terrainType) {
        if (terrainType == getAdvantageTerrainType()) {
            return TERRAIN_ADVANTAGE_BONUS;
        }
        if (terrainType == getDisadvantageTerrainType()) {
            return TERRAIN_DISADVANTAGE_PENALTY;
        }
        return 0;
    }

    public boolean hasActedThisRound() {
        return actedThisRound;
    }

    public void markActedThisRound() {
        if (actedThisRound) {
            throw new IllegalStateException("Unit has already acted this round.");
        }
        actedThisRound = true;
    }

    public void resetRoundActionState() {
        actedThisRound = false;
    }
}
