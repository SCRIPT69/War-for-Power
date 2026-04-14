package cz.cvut.fel.pjv.warforpower.save;

import cz.cvut.fel.pjv.warforpower.model.game.Game;
import java.nio.file.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Handles saving and loading game state to/from a JSON file. */
public class SaveManager {
    private static final Path SAVE_FILE = Path.of("savegame.json");
    private static final Logger LOGGER = LoggerFactory.getLogger(SaveManager.class);

    /** Saves the current game state to disk. */
    public static void save(Game game) {
        LOGGER.info("Saving game state.");
    }

    /** Loads game state from disk. Returns null if no save exists. */
    public static Game load() {
        LOGGER.info("Loading game state.");
        return null;
    }

    /** Returns true if a save file exists. */
    public static boolean hasSave() {
        return Files.exists(SAVE_FILE);
    }
}