package cz.cvut.fel.pjv.warforpower.model.tiles;


public abstract class HexTile {
    private final HexTileCoords tileCoords;
    private final HexTileType tileType;

    public HexTile(HexTileCoords tileCoords, HexTileType tileType) {
        if (tileCoords == null) {
            throw new IllegalArgumentException("tileCoords cannot be null.");
        }
        if (tileType == null) {
            throw new IllegalArgumentException("tileType cannot be null.");
        }

        this.tileCoords = tileCoords;
        this.tileType = tileType;
    }

    public HexTileType getTileType() {
        return tileType;
    }

    public HexTileCoords getTileCoords() {
        return tileCoords;
    }

    public int getRowIndex() {
        return tileCoords.rowIndex();
    }

    public int getTileIndex() {
        return tileCoords.tileIndex();
    }
}
