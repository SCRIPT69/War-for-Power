package cz.cvut.fel.pjv.warforpower.view.game.battle;

import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.List;

/**
 * Immutable data used to populate battle overlay view.
 *
 * @param title overlay title
 * @param attackerLabel label of attacking side
 * @param defenderLabel label of defending side
 * @param attackers attacking units
 * @param defenders defending units
 * @param defenderIsCity true if defending side represents a city
 */
public record BattleOverlayData(
        String title,
        String attackerLabel,
        String defenderLabel,
        List<Unit> attackers,
        List<Unit> defenders,
        boolean defenderIsCity
) {
    public BattleOverlayData {
        if (title == null || attackerLabel == null || defenderLabel == null) {
            throw new IllegalArgumentException("Battle overlay text fields cannot be null.");
        }
        if (attackers == null || defenders == null) {
            throw new IllegalArgumentException("Battle overlay unit lists cannot be null.");
        }
    }
}