package cz.cvut.fel.pjv.warforpower.save;

/**
 * Snapshot of a single map tile state.
 */
public record TileSnapshot(
        int rowIndex,
        int tileIndex,
        String tileType,
        String terrainType,
        Integer ownerIndex,
        boolean unitBoughtThisRound,
        boolean capturedThisRound
) {
}