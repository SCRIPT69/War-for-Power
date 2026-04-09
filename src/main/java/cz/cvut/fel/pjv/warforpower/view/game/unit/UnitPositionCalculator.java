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
    private static final double TWO_UNIT_OFFSET = 14;
    /** Small downward shift when only one unit is on a tile. */
    private static final double ONE_UNIT_Y_SHIFT = 6;

    private UnitPositionCalculator() {}

    /**
     * Returns screen positions for 1 or 2 units standing on a tile.
     * One unit: centered slightly below tile center.
     * Two units: one above center, one below center.
     *
     * @param tilePosition top-left corner of the tile
     * @param unitCount    number of units (1 or 2)
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
     * Returns screen positions for units displayed during battle.
     * Attacker units are placed on the left side, defender units on the right.
     *
     * @param centerX      horizontal center of the battle area
     * @param centerY      vertical center of the battle area
     * @param attackerCount number of attacker units (1 or 2)
     * @param defenderCount number of defender units (1 or 2)
     * @return array of two lists: [0] = attacker positions, [1] = defender positions
     */
    public static UnitsPositionsInBattle forBattle(
            double centerX, double centerY,
            int attackerCount, int defenderCount) {

        double sideOffset = 60;
        double unitSpread = 22;

        List<ScreenPosition> attackerPositions = buildSidePositions(
                centerX - sideOffset, centerY, attackerCount, unitSpread);
        List<ScreenPosition> defenderPositions = buildSidePositions(
                centerX + sideOffset, centerY, defenderCount, unitSpread);

        return new UnitsPositionsInBattle(attackerPositions, defenderPositions);
    }

    /**
     * Builds a vertical column of unit positions centered on (x, centerY).
     *
     * @param x        horizontal position
     * @param centerY  vertical center
     * @param count    number of units (1 or 2)
     * @param spread   vertical distance between units
     * @return list of positions
     */
    private static List<ScreenPosition> buildSidePositions(
            double x, double centerY, int count, double spread) {
        if (count < 1 || count > 2) {
            throw new IllegalArgumentException("Unit count must be 1 or 2.");
        }

        if (count == 1) {
            return List.of(new ScreenPosition(x, centerY));
        }
        return List.of(
                new ScreenPosition(x, centerY - spread),
                new ScreenPosition(x, centerY + spread)
        );
    }
}