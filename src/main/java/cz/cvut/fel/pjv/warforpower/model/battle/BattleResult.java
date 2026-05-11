package cz.cvut.fel.pjv.warforpower.model.battle;

import cz.cvut.fel.pjv.warforpower.model.tiles.HexTile;

/**
 * Stores complete resolved battle data for both game logic
 * and battle presentation in the user interface.
 *
 * @param tileOfBattle tile on which the battle took place
 * @param firstAttempt first resolved battle attempt
 * @param secondAttempt second resolved battle attempt, null if no reroll happened
 * @param finalOutcome final battle outcome after all attempts
 */
public record BattleResult(
        HexTile tileOfBattle,
        BattleAttemptResult firstAttempt,
        BattleAttemptResult secondAttempt,
        BattleOutcome finalOutcome
) {
    public BattleResult {
        if (tileOfBattle == null || firstAttempt == null || finalOutcome == null) {
            throw new IllegalArgumentException("Battle result fields cannot be null.");
        }
    }

    /**
     * Returns whether a second attempt exists.
     *
     * @return true if battle had a reroll attempt
     */
    public boolean hasSecondAttempt() {
        return secondAttempt != null;
    }
}