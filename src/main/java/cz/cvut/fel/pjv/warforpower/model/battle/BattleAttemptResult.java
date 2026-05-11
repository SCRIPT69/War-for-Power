package cz.cvut.fel.pjv.warforpower.model.battle;

/**
 * Stores one resolved battle attempt.
 *
 * @param attackerResult attacker side result of this attempt
 * @param defenderResult defender side result of this attempt
 * @param battleOutcome outcome of this attempt
 */
public record BattleAttemptResult(
        BattleSideResult attackerResult,
        BattleSideResult defenderResult,
        BattleOutcome battleOutcome
) {
    public BattleAttemptResult {
        if (attackerResult == null || defenderResult == null || battleOutcome == null) {
            throw new IllegalArgumentException("Battle attempt result fields cannot be null.");
        }
    }

    /**
     * Returns whether this attempt ended in a draw.
     *
     * @return true if this attempt ended in a draw
     */
    public boolean isDraw() {
        return battleOutcome == BattleOutcome.DRAW;
    }
}