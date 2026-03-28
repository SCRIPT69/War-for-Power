package cz.cvut.fel.pjv.warforpower.model.map;

import cz.cvut.fel.pjv.warforpower.model.game.Game;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.tiles.*;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GameMap {
    private final List<List<HexTile>> map;
    private static final int[] ROW_LENGTHS = {5, 6, 7, 8, 9, 8, 7, 6, 5};
    private static final int ROWS_NUMBER = ROW_LENGTHS.length;
    private static final int TILES_PER_TERRAIN_TYPE = 14;

    public GameMap() {
        this.map = new ArrayList<>();
    }

    public void generateMap(Player[] players) {
        initializeEmptyMap();
        setBasesAndCities(players);
        setTerrainTiles();
    }

    private void initializeEmptyMap() {
        map.clear();

        for (int i = 0; i < ROWS_NUMBER; i++) {
            ArrayList<HexTile> row = new ArrayList<>(ROW_LENGTHS[i]);

            for (int j = 0; j < ROW_LENGTHS[i]; j++) {
                row.add(null);
            }

            map.add(row);
        }
    }

    private void setTile(HexTileCoords tileCoords, HexTile tile) {
        validateCoords(tileCoords);
        map.get(tileCoords.rowIndex()).set(tileCoords.tileIndex(), tile);
    }

    public HexTile getTile(HexTileCoords tileCoords) {
        validateCoords(tileCoords);
        return map.get(tileCoords.rowIndex()).get(tileCoords.tileIndex());
    }

    public int getRowLength(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= ROWS_NUMBER) {
            throw new IllegalArgumentException("Invalid row index.");
        }
        return ROW_LENGTHS[rowIndex];
    }

    public int getRowsNumber() {
        return ROWS_NUMBER;
    }

    private void validateCoords(HexTileCoords tileCoords) {
        if (!isValidCoords(tileCoords)) {
            throw new IllegalArgumentException("Invalid tile coordinates");
        }
    }

    public boolean isValidCoords(HexTileCoords tileCoords) {
        int rowIndex = tileCoords.rowIndex();
        int tileIndex = tileCoords.tileIndex();
        return rowIndex >= 0 && rowIndex < ROWS_NUMBER
                && tileIndex >= 0 && tileIndex < ROW_LENGTHS[rowIndex];
    }

    public int countUnitsOfPlayer(Player player) {
        int count = 0;

        for (List<HexTile> row : map) {
            for (HexTile tile : row) {
                if (tile instanceof OccupiableTile occupiableTile) {
                    for (Unit unit : occupiableTile.getStandingUnits()) {
                        if (unit.getOwner().equals(player)) {
                            count++;
                        }
                    }
                }
            }
        }

        return count;
    }

    public List<BaseTile> getAllBases() {
        List<BaseTile> bases = new ArrayList<>();
        for (List<HexTile> row : map) {
            for (HexTile tile : row) {
                if (tile instanceof BaseTile baseTile) {
                    bases.add(baseTile);
                }
            }
        }
        return bases;
    }

    private void setBasesAndCities(Player[] players) {
        if (players.length < 2 || players.length > 4) {
            throw new IllegalArgumentException("Invalid number of players");
        }

        HexTileCoords[] allBasesCoords = {
                new HexTileCoords(0, 2), // top
                new HexTileCoords(8, 2), // bottom
                new HexTileCoords(4, 0), // left
                new HexTileCoords(4, 8), // right
        };

        HexTileCoords[] basesCoords = resolveBaseCoords(players, allBasesCoords);

        //setting bases
        for (int i = 0; i < players.length; i++) {
            setTile(basesCoords[i], new BaseTile(basesCoords[i], players[i]));
            players[i].increaseBasesCount();
        }

        //setting cities
        HexTileCoords cityInCenterCoords = new HexTileCoords(4, 4);
        setTile(cityInCenterCoords, new CityTile(cityInCenterCoords));
        // remaining base positions become cities
        for (HexTileCoords coords : allBasesCoords) {
            if (getTile(coords) == null) {
                setTile(coords, new CityTile(coords));
            }
        }
    }
    private HexTileCoords[] resolveBaseCoords(Player[] players, HexTileCoords[] allBasesCoords) {
        HexTileCoords[] basesCoords = new HexTileCoords[players.length];

        if (players.length == 2) {
            List<HexTileCoords> topBottomBases = Arrays.asList(
                    allBasesCoords[0],
                    allBasesCoords[1]
            );
            Collections.shuffle(topBottomBases);

            basesCoords[0] = topBottomBases.get(0);
            basesCoords[1] = topBottomBases.get(1);
        }
        else {
            List<HexTileCoords> shuffledBases = new ArrayList<>(Arrays.asList(allBasesCoords));
            Collections.shuffle(shuffledBases);

            for (int i = 0; i < players.length; i++) {
                basesCoords[i] = shuffledBases.get(i);
            }
        }

        return basesCoords;
    }

    private void setTerrainTiles() {
        List<TerrainType> terrainPool = new ArrayList<>();
        for (int i = 0; i < TILES_PER_TERRAIN_TYPE; i++) {
            terrainPool.add(TerrainType.DESERT);
            terrainPool.add(TerrainType.MOUNTAIN);
            terrainPool.add(TerrainType.PLAINS);
            terrainPool.add(TerrainType.FOREST);
        }
        Collections.shuffle(terrainPool);

        int terrainIndex = 0;
        for (int rowIndex = 0; rowIndex < ROWS_NUMBER; rowIndex++) {
            for (int tileIndex = 0; tileIndex < ROW_LENGTHS[rowIndex]; tileIndex++) {
                HexTileCoords tileCoords = new HexTileCoords(rowIndex, tileIndex);

                if (getTile(tileCoords) == null) {
                    TerrainType terrainType = terrainPool.get(terrainIndex);
                    setTile(tileCoords, new TerrainTile(tileCoords, terrainType));
                    terrainIndex++;
                }
            }
        }
        if (terrainIndex != terrainPool.size()) {
            throw new IllegalStateException("Terrain pool was not fully used.");
        }
    }
}
