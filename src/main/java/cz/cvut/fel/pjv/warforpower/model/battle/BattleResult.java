package cz.cvut.fel.pjv.warforpower.model.battle;

import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;

public record BattleResult(
        OccupiableTile tileOfBattle,
        BattleSideResult attackerResult,
        BattleSideResult defenderResult,
        BattleOutcome battleOutcome
) {
}
