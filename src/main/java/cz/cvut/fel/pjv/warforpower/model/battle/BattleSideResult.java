package cz.cvut.fel.pjv.warforpower.model.battle;

import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.List;

public record BattleSideResult(
        List<Unit> units,
        DiceRoll rolls,
        int bonusPoints
) {
    public int getRollsSum() {
        return rolls.getSum();
    }

    public int getTotalPoints() {
        return getRollsSum() + bonusPoints;
    }
}