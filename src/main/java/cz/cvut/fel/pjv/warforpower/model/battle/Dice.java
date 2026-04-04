package cz.cvut.fel.pjv.warforpower.model.battle;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for generating dice rolls used in battle resolution.
 */
public class Dice {
    private Dice() {
    }

    /**
     * Rolls two six-sided dice.
     *
     * @return result of two dice rolls
     */
    public static DiceRoll getTwoDiceResults() {
        int dice1 = ThreadLocalRandom.current().nextInt(1, 7);
        int dice2 = ThreadLocalRandom.current().nextInt(1, 7);

        return new DiceRoll(dice1, dice2);
    }
}
