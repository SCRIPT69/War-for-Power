package cz.cvut.fel.pjv.warforpower.save;

/**
 * Snapshot of a single unit state.
 */
public record UnitSnapshot(
        String unitType,
        int ownerIndex,
        int rowIndex,
        int tileIndex,
        boolean mainActionUsed
) {
}