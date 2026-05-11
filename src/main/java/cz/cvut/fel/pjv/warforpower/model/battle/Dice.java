package cz.cvut.fel.pjv.warforpower.model.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for generating dice rolls used in battle resolution.
 */
public class Dice {
    private Dice() {
    }

    /**
     * Rolls the specified number of six-sided dice.
     *
     * @param diceCount number of dice to roll
     * @return result of the dice rolls
     */
    public static DiceRoll rollDice(int diceCount) {
        if (diceCount <= 0) {
            throw new IllegalArgumentException("Dice count must be positive.");
        }

        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < diceCount; i++) {
            values.add(ThreadLocalRandom.current().nextInt(1, 7));
        }

        return new DiceRoll(values);
    }
}