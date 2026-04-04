package cz.cvut.fel.pjv.warforpower.save;

import cz.cvut.fel.pjv.warforpower.model.game.Game;
import java.nio.file.*;

/** Handles saving and loading game state to/from a JSON file. */
public class SaveManager {
    private static final Path SAVE_FILE = Path.of("savegame.json");

    /** Saves the current game state to disk. */
    public static void save(Game game) { }

    /** Loads game state from disk. Returns null if no save exists. */
    public static Game load() { return null; }

    /** Returns true if a save file exists. */
    public static boolean hasSave() {
        return Files.exists(SAVE_FILE);
    }
}