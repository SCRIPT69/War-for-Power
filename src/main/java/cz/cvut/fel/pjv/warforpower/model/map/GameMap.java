package cz.cvut.fel.pjv.warforpower.model.map;

import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.tiles.*;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents the main game map, stores all map tiles
 * and provides map-related queries and helper methods.
 */
public class GameMap {
    private final List<List<HexTile>> map;
    private static final int[] ROW_LENGTHS = {5, 6, 7, 8, 9, 8, 7, 6, 5};
    private static final int ROWS_NUMBER = ROW_LENGTHS.length;
    private static final int TILES_PER_TERRAIN_TYPE = 14;

    public GameMap() {
        this.map = new ArrayList<>();
    }

    /**
     * Generates a new game map for the given players.
     * The generation includes bases, cities and terrain tiles.
     *
     * @param players players participating in the game
     */
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

    /**
     * Replaces the tile at the specified coordinates with a new tile.
     *
     * @param tileCoords coordinates of the tile to replace
     * @param newTile replacement tile
     */
    public void replaceTile(HexTileCoords tileCoords, HexTile newTile) {
        if (tileCoords == null) {
            throw new IllegalArgumentException("Tile coordinates cannot be null.");
        }
        if (newTile == null) {
            throw new IllegalArgumentException("New tile cannot be null.");
        }

        setTile(tileCoords, newTile);
    }

    /**
     * Returns the tile at the specified coordinates.
     *
     * @param tileCoords coordinates of the tile
     * @return tile located at the given coordinates
     * @throws IllegalArgumentException if the coordinates are invalid
     */
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

    /**
     * Checks whether the given coordinates exist on the map.
     *
     * @param tileCoords coordinates to validate
     * @return true if the coordinates are valid
     */
    public boolean isValidCoords(HexTileCoords tileCoords) {
        int rowIndex = tileCoords.rowIndex();
        int tileIndex = tileCoords.tileIndex();
        return rowIndex >= 0 && rowIndex < ROWS_NUMBER
                && tileIndex >= 0 && tileIndex < ROW_LENGTHS[rowIndex];
    }

    /**
     * Counts all units on the map belonging to the specified player.
     *
     * @param player player whose units should be counted
     * @return number of units owned by the player
     */
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

    /**
     * Returns all units currently present on the map.
     *
     * @return list of all units on the map
     */
    public List<Unit> getAllUnits() {
        List<Unit> units = new ArrayList<>();

        for (List<HexTile> row : map) {
            for (HexTile tile : row) {
                if (tile instanceof OccupiableTile occupiableTile) {
                    units.addAll(occupiableTile.getStandingUnits());
                }
            }
        }

        return units;
    }

    /**
     * Returns all base tiles currently present on the map.
     *
     * @return list of all bases
     */
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

    /**
     * Returns all bases owned by the given player.
     *
     * @param player owner of requested bases
     * @return list of player's bases
     */
    public List<BaseTile> getBasesOfPlayer(Player player) {
        List<BaseTile> basesOfPlayer = new ArrayList<>();
        for (List<HexTile> row : map) {
            for (HexTile tile : row) {
                if (tile instanceof BaseTile baseTile && baseTile.getOwner() == player) {
                    basesOfPlayer.add(baseTile);
                }
            }
        }
        return basesOfPlayer;
    }

    /**
     * Returns all neighbouring tiles of the specified tile coordinates.
     *
     * @param currentTileCoords coordinates of the reference tile
     * @return list of neighbouring tiles
     */
    public List<HexTile> getNeighbourTiles(HexTileCoords currentTileCoords) {
        List<HexTile> neighbours = new ArrayList<>();

        int rowIndex = currentTileCoords.rowIndex();
        int tileIndex = currentTileCoords.tileIndex();

        // same row
        addTileIfValid(neighbours, new HexTileCoords(rowIndex, tileIndex - 1));
        addTileIfValid(neighbours, new HexTileCoords(rowIndex, tileIndex + 1));

        if (rowIndex < 4) {
            addTileIfValid(neighbours, new HexTileCoords(rowIndex - 1, tileIndex - 1));
            addTileIfValid(neighbours, new HexTileCoords(rowIndex - 1, tileIndex));
            addTileIfValid(neighbours, new HexTileCoords(rowIndex + 1, tileIndex));
            addTileIfValid(neighbours, new HexTileCoords(rowIndex + 1, tileIndex + 1));
        } else if (rowIndex == 4) {
            addTileIfValid(neighbours, new HexTileCoords(rowIndex - 1, tileIndex - 1));
            addTileIfValid(neighbours, new HexTileCoords(rowIndex - 1, tileIndex));
            addTileIfValid(neighbours, new HexTileCoords(rowIndex + 1, tileIndex - 1));
            addTileIfValid(neighbours, new HexTileCoords(rowIndex + 1, tileIndex));
        } else {
            addTileIfValid(neighbours, new HexTileCoords(rowIndex - 1, tileIndex));
            addTileIfValid(neighbours, new HexTileCoords(rowIndex - 1, tileIndex + 1));
            addTileIfValid(neighbours, new HexTileCoords(rowIndex + 1, tileIndex - 1));
            addTileIfValid(neighbours, new HexTileCoords(rowIndex + 1, tileIndex));
        }

        return neighbours;
    }
    /**
     * Adds the tile at the given coordinates to the provided list
     * if the coordinates are valid.
     *
     * @param tiles output list of tiles
     * @param coords coordinates to inspect
     */
    private void addTileIfValid(List<HexTile> tiles, HexTileCoords coords) {
        if (!isValidCoords(coords)) {
            return;
        }

        tiles.add(getTile(coords));
    }
    /**
     * Returns all neighbouring occupiable tiles of the specified tile coordinates.
     *
     * @param currentTileCoords coordinates of the reference tile
     * @return list of neighbouring occupiable tiles
     */
    public List<OccupiableTile> getNeighbourOccupiableTiles(HexTileCoords currentTileCoords) {
        List<OccupiableTile> neighbours = new ArrayList<>();

        for (HexTile tile : getNeighbourTiles(currentTileCoords)) {
            if (tile instanceof OccupiableTile occupiableTile) {
                neighbours.add(occupiableTile);
            }
        }

        return neighbours;
    }

    /**
     * Places bases and cities onto the newly generated map.
     *
     * @param players players participating in the game
     */
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
    /**
     * Resolves random starting base coordinates for players
     * while respecting the special two-player placement rule.
     *
     * @param players players participating in the game
     * @param allBasesCoords all predefined base positions
     * @return selected base coordinates for all players
     */
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

    /**
     * Fills all remaining free tiles with randomized terrain tiles.
     */
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
