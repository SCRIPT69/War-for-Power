package cz.cvut.fel.pjv.warforpower.save;

import java.util.List;

/**
 * Flat snapshot of complete game state used for saving and loading.
 */
public record GameSnapshot(
        int currentRound,
        int currentPlayerIndex,
        List<PlayerSnapshot> players,
        List<TileSnapshot> tiles,
        List<UnitSnapshot> units
) {
}