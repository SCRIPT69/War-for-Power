package cz.cvut.fel.pjv.warforpower.view.game.unit;

import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;

import java.util.List;

public record UnitsPositionsInBattle(
        List<ScreenPosition> attackerPositions,
        List<ScreenPosition> defenderPositions
) {
}
