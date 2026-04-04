package cz.cvut.fel.pjv.warforpower.model.units;

import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainType;

/**
 * Enumerates all supported unit types and stores
 * their terrain advantage, disadvantage and price.
 */
public enum UnitType {
    INFANTRY(TerrainType.FOREST, TerrainType.PLAINS),
    ARCHERS(TerrainType.DESERT, TerrainType.FOREST),
    CAVALRY(TerrainType.PLAINS, TerrainType.MOUNTAIN),
    ARTILLERY(TerrainType.MOUNTAIN, TerrainType.DESERT);

    private final int price = 100;
    private final TerrainType advantageTerrainType;
    private final TerrainType disadvantageTerrainType;

    UnitType(TerrainType advantageTerrainType, TerrainType disadvantageTerrainType) {
        this.advantageTerrainType = advantageTerrainType;
        this.disadvantageTerrainType = disadvantageTerrainType;
    }

    /**
     * Returns the purchase price of this unit type.
     *
     * @return unit price
     */
    public int getPrice() {
        return price;
    }

    /**
     * Returns the advantageous terrain type of this unit type.
     *
     * @return advantageous terrain
     */
    public TerrainType getAdvantageTerrainType() {
        return advantageTerrainType;
    }

    /**
     * Returns the disadvantageous terrain type of this unit type.
     *
     * @return disadvantageous terrain
     */
    public TerrainType getDisadvantageTerrainType() {
        return disadvantageTerrainType;
    }
}
