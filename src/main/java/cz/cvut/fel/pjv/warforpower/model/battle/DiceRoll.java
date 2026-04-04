package cz.cvut.fel.pjv.warforpower.model.battle;

public record DiceRoll(int first, int second) {
    public int getSum() {
        return first + second;
    }
}
