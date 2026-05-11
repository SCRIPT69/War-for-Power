package cz.cvut.fel.pjv.warforpower.model.battle;

import java.util.List;

/**
 * Represents the result of rolling one or more dice.
 */
public record DiceRoll(List<Integer> values) {
    public DiceRoll {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("Dice values cannot be null or empty.");
        }
    }

    /**
     * Returns the sum of all dice values.
     *
     * @return sum of rolled dice
     */
    public int getSum() {
        return values.stream().mapToInt(Integer::intValue).sum();
    }
}