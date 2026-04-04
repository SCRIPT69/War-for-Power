package cz.cvut.fel.pjv.warforpower.model.tiles;

/**
 * A special map tile representing a city.
 * Cities do not recruit units directly and may have custom capture rules.
 */
public class CityTile extends HexTile {
    public CityTile(HexTileCoords tileCoords) {
        super(tileCoords, HexTileType.CITY);
    }
}