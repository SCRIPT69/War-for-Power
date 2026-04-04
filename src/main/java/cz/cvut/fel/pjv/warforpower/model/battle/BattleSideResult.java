package cz.cvut.fel.pjv.warforpower.model.battle;

import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.List;

/**
 * Stores battle data for one side of the conflict,
 * including participating units, dice rolls and bonus points.
 */
public record BattleSideResult(
        List<Unit> units,
        DiceRoll rolls,
        int bonusPoints
) {
    /**
     * Returns the sum of dice rolls of this battle side.
     *
     * @return sum of dice rolls
     */
    public int getRollsSum() {
        return rolls.getSum();
    }

    /**
     * Returns total battle points including bonus points.
     *
     * @return total points
     */
    public int getTotalPoints() {
        return getRollsSum() + bonusPoints;
    }
}