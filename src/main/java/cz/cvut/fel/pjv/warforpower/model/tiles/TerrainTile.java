package cz.cvut.fel.pjv.warforpower.model.tiles;

import cz.cvut.fel.pjv.warforpower.model.players.Player;

public class TerrainTile extends OccupiableTile implements Ownable {
    private final TerrainType terrainType;
    private Player owner;

    public TerrainTile(HexTileCoords tileCoords, TerrainType terrainType) {
        super(tileCoords, HexTileType.TERRAIN);
        this.terrainType = terrainType;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Player owner) {
        this.owner = owner;
    }
}
