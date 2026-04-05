package cz.cvut.fel.pjv.warforpower.model.battle;

import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.List;

/**
 * Stores battle data of one side, including participating units,
 * dice rolls, bonus points and units lost in the battle.
 */
public record BattleSideResult(
        List<Unit> units,
        DiceRoll rolls,
        int bonusPoints,
        List<Unit> lostUnits
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