package cz.cvut.fel.pjv.warforpower.view.game.unit;

import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import java.util.List;

/**
 * Calculates screen positions for unit icons on a tile or in the battle view.
 */
public final class UnitPositionCalculator {

    /** Half-width of a tile in pixels. */
    private static final double TILE_HALF_WIDTH = 35.5;
    /** Half-height of a tile in pixels. */
    private static final double TILE_HALF_HEIGHT = 40;

    /** Vertical offset between two units on the same tile. */
    private static final double TWO_UNIT_OFFSET = 18;
    /** Small downward shift when only one unit is on a tile. */
    private static final double ONE_UNIT_Y_SHIFT = 6;

    /** Horizontal offset of attackers/defenders during battle-on-tile rendering. */
    private static final double BATTLE_SIDE_OFFSET = 16;

    /** Vertical distance between two units on one battle side. */
    private static final double BATTLE_UNIT_SPREAD = 14;

    /** Moves the whole two-unit battle column slightly upward. */
    private static final double BATTLE_TWO_UNITS_Y_SHIFT = -8;

    private UnitPositionCalculator() {}

    /**
     * Returns screen positions for 1 or 2 units standing on a tile.
     * One unit: centered slightly below tile center.
     * Two units: one above center, one below center.
     *
     * @param tilePosition top-left corner of the tile
     * @param unitCount number of units (1 or 2)
     * @return list of screen positions, same size as unitCount
     */
    public static List<ScreenPosition> forTile(ScreenPosition tilePosition, int unitCount) {
        if (tilePosition == null) {
            throw new IllegalArgumentException("Tile position cannot be null.");
        }
        if (unitCount < 1 || unitCount > 2) {
            throw new IllegalArgumentException("Unit count must be 1 or 2.");
        }

        double cx = tilePosition.x() + TILE_HALF_WIDTH;
        double cy = tilePosition.y() + TILE_HALF_HEIGHT;

        if (unitCount == 1) {
            return List.of(new ScreenPosition(cx, cy + ONE_UNIT_Y_SHIFT));
        }

        return List.of(
                new ScreenPosition(cx, cy - TWO_UNIT_OFFSET),
                new ScreenPosition(cx, cy + TWO_UNIT_OFFSET)
        );
    }

    /**
     * Returns screen positions for units displayed in battle state on a tile.
     * Attackers are placed on the left side, defenders on the right.
     *
     * @param centerX horizontal center of the tile
     * @param centerY vertical center of the tile
     * @param attackerCount number of attacker units (0..2)
     * @param defenderCount number of defender units (0..2)
     * @return attacker and defender positions
     */
    public static UnitsPositionsInBattle forBattle(
            double centerX,
            double centerY,
            int attackerCount,
            int defenderCount) {

        List<ScreenPosition> attackerPositions = buildSidePositions(
                centerX - BATTLE_SIDE_OFFSET,
                centerY,
                attackerCount,
                BATTLE_UNIT_SPREAD
        );

        List<ScreenPosition> defenderPositions = buildSidePositions(
                centerX + BATTLE_SIDE_OFFSET,
                centerY,
                defenderCount,
                BATTLE_UNIT_SPREAD
        );

        return new UnitsPositionsInBattle(attackerPositions, defenderPositions);
    }

    /**
     * Builds a vertical column of unit positions centered near (x, centerY).
     *
     * @param x horizontal position
     * @param centerY vertical center
     * @param count number of units (0..2)
     * @param spread vertical distance between two units
     * @return list of positions
     */
    private static List<ScreenPosition> buildSidePositions(
            double x,
            double centerY,
            int count,
            double spread) {

        if (count < 0 || count > 2) {
            throw new IllegalArgumentException("Unit count must be between 0 and 2.");
        }

        if (count == 0) {
            return List.of();
        }

        if (count == 1) {
            return List.of(new ScreenPosition(x, centerY));
        }

        double shiftedCenterY = centerY + BATTLE_TWO_UNITS_Y_SHIFT;

        return List.of(
                new ScreenPosition(x, shiftedCenterY - spread),
                new ScreenPosition(x, shiftedCenterY + spread)
        );
    }
}