package cz.cvut.fel.pjv.warforpower.model.battle;

/**
 * Represents the result of rolling two dice.
 */
public record DiceRoll(int first, int second) {
    public int getSum() {
        return first + second;
    }
}
