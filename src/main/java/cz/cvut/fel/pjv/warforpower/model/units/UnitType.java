package cz.cvut.fel.pjv.warforpower.model.units;

import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainType;

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

    public int getPrice() {
        return price;
    }

    public TerrainType getAdvantageTerrainType() {
        return advantageTerrainType;
    }

    public TerrainType getDisadvantageTerrainType() {
        return disadvantageTerrainType;
    }
}
