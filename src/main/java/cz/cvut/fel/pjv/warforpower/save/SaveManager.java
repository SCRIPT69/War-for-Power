package cz.cvut.fel.pjv.warforpower.save;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cz.cvut.fel.pjv.warforpower.model.game.Game;
import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles saving and loading game state to and from a JSON file.
 *
 * The game is saved through a flat DTO snapshot instead of serializing
 * model objects directly. This avoids circular references between units,
 * tiles and the map.
 */
public class SaveManager {
    private static final Path SAVE_FILE = Path.of("savegame.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger(SaveManager.class);

    /**
     * Saves current game state to disk.
     *
     * @param game game instance to save
     */
    public static void save(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }

        try {
            GameSnapshot snapshot = toSnapshot(game);
            String json = GSON.toJson(snapshot);

            Files.writeString(SAVE_FILE, json);

            LOGGER.info("Game saved to {}.", SAVE_FILE.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("Failed to save game.", e);
            throw new IllegalStateException("Failed to save game.", e);
        }
    }

    /**
     * Loads game state from disk.
     *
     * @return restored game, or null if no save file exists
     */
    public static Game load() {
        if (!hasSave()) {
            LOGGER.info("No save file found.");
            return null;
        }

        try {
            String json = Files.readString(SAVE_FILE);
            GameSnapshot snapshot = GSON.fromJson(json, GameSnapshot.class);

            Game game = new Game(snapshot.players().size());
            game.restoreFromSnapshot(snapshot);

            LOGGER.info("Game loaded from {}.", SAVE_FILE.toAbsolutePath());

            return game;
        } catch (IOException | RuntimeException e) {
            LOGGER.error("Failed to load game.", e);
            throw new IllegalStateException("Failed to load game.", e);
        }
    }

    /**
     * Returns true if a save file exists.
     *
     * @return true if save file exists
     */
    public static boolean hasSave() {
        return Files.exists(SAVE_FILE);
    }

    /**
     * Deletes existing save file if it exists.
     */
    public static void deleteSave() {
        try {
            Files.deleteIfExists(SAVE_FILE);
            LOGGER.info("Save file deleted.");
        } catch (IOException e) {
            LOGGER.error("Failed to delete save file.", e);
            throw new IllegalStateException("Failed to delete save file.", e);
        }
    }

    private static GameSnapshot toSnapshot(Game game) {
        Player[] players = game.getPlayers();

        List<PlayerSnapshot> playerSnapshots = new ArrayList<>();
        for (Player player : players) {
            playerSnapshots.add(toPlayerSnapshot(player));
        }

        List<TileSnapshot> tileSnapshots = new ArrayList<>();
        List<UnitSnapshot> unitSnapshots = new ArrayList<>();

        GameMap gameMap = game.getGameMap();

        for (int rowIndex = 0; rowIndex < GameMap.ROWS_NUMBER; rowIndex++) {
            for (int tileIndex = 0; tileIndex < GameMap.ROW_LENGTHS[rowIndex]; tileIndex++) {
                HexTileCoords coords = new HexTileCoords(rowIndex, tileIndex);
                HexTile tile = gameMap.getTile(coords);

                tileSnapshots.add(toTileSnapshot(tile, players));

                if (tile instanceof OccupiableTile occupiableTile) {
                    for (Unit unit : occupiableTile.getStandingUnits()) {
                        unitSnapshots.add(toUnitSnapshot(unit, players));
                    }
                }
            }
        }

        return new GameSnapshot(
                game.getCurrentRound(),
                game.getCurrentPlayerIndex(),
                playerSnapshots,
                tileSnapshots,
                unitSnapshots
        );
    }

    private static PlayerSnapshot toPlayerSnapshot(Player player) {
        return new PlayerSnapshot(
                player.getName(),
                player.getColor().name(),
                player.getMoney(),
                player.getBasesCount(),
                player.isEliminated()
        );
    }

    private static TileSnapshot toTileSnapshot(HexTile tile, Player[] players) {
        HexTileCoords coords = tile.getTileCoords();

        String tileType = tile.getTileType().name();
        String terrainType = null;
        Integer ownerIndex = null;
        boolean unitBoughtThisRound = false;
        boolean capturedThisRound = false;

        if (tile instanceof TerrainTile terrainTile) {
            terrainType = terrainTile.getTerrainType().name();
            ownerIndex = indexOf(terrainTile.getOwner(), players);
        } else if (tile instanceof BaseTile baseTile) {
            ownerIndex = indexOf(baseTile.getOwner(), players);
            unitBoughtThisRound = baseTile.hasUnitBoughtThisRound();
            capturedThisRound = baseTile.hasBeenCapturedThisRound();
        }

        return new TileSnapshot(
                coords.rowIndex(),
                coords.tileIndex(),
                tileType,
                terrainType,
                ownerIndex,
                unitBoughtThisRound,
                capturedThisRound
        );
    }

    private static UnitSnapshot toUnitSnapshot(Unit unit, Player[] players) {
        HexTileCoords coords = unit.getOccupiedTile().getTileCoords();

        return new UnitSnapshot(
                unit.getUnitType().name(),
                indexOfRequired(unit.getOwner(), players),
                coords.rowIndex(),
                coords.tileIndex(),
                unit.hasUsedMainActionThisRound()
        );
    }

    private static Integer indexOf(Player player, Player[] players) {
        if (player == null) {
            return null;
        }

        for (int i = 0; i < players.length; i++) {
            if (players[i] == player) {
                return i;
            }
        }

        return null;
    }

    private static int indexOfRequired(Player player, Player[] players) {
        Integer index = indexOf(player, players);

        if (index == null) {
            throw new IllegalStateException("Player was not found in game players array.");
        }

        return index;
    }
}