package cz.cvut.fel.pjv.warforpower.model.battle;

import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;

/**
 * Stores complete resolved battle data for both game logic
 * and battle presentation in the user interface.
 */
public record BattleResult(
        OccupiableTile tileOfBattle,
        BattleSideResult attackerResult,
        BattleSideResult defenderResult,
        BattleOutcome battleOutcome
) {
}
