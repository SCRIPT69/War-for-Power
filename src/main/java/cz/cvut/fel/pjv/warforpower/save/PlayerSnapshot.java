package cz.cvut.fel.pjv.warforpower.save;

/**
 * Snapshot of a single player state.
 */
public record PlayerSnapshot(
        String name,
        String color,
        int money,
        int basesCount,
        boolean eliminated
) {
}