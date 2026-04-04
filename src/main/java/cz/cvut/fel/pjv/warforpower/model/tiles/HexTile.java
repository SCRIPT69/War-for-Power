package cz.cvut.fel.pjv.warforpower.model.tiles;


/**
 * Abstract base class for all map tiles.
 * Every tile has fixed coordinates and a tile type.
 */
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

    /**
     * Returns the row index of this tile.
     *
     * @return row index
     */
    public int getRowIndex() {
        return tileCoords.rowIndex();
    }

    /**
     * Returns the tile index within its row.
     *
     * @return tile index
     */
    public int getTileIndex() {
        return tileCoords.tileIndex();
    }
}
