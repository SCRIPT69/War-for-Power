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
 * @param attackerRolls attacker dice values
 * @param defenderRolls defender dice values
 * @param attackerBonus attacker bonus points
 * @param defenderBonus defender bonus points
 * @param attackerTotal attacker total points
 * @param defenderTotal defender total points
 * @param resultText textual result of the battle
 * @param draw true if the battle attempt ended in draw
 * @param canReroll true if another reroll is still allowed
 */
public record BattleOverlayData(
        String title,
        String attackerLabel,
        String defenderLabel,
        List<Unit> attackers,
        List<Unit> defenders,
        boolean defenderIsCity,
        List<Integer> attackerRolls,
        List<Integer> defenderRolls,
        int attackerBonus,
        int defenderBonus,
        int attackerTotal,
        int defenderTotal,
        String resultText,
        boolean draw,
        boolean canReroll
) {
    public BattleOverlayData {
        if (title == null || attackerLabel == null || defenderLabel == null) {
            throw new IllegalArgumentException("Battle overlay text fields cannot be null.");
        }
        if (attackers == null || defenders == null) {
            throw new IllegalArgumentException("Battle overlay unit lists cannot be null.");
        }
        if (attackerRolls == null || defenderRolls == null) {
            throw new IllegalArgumentException("Battle overlay dice lists cannot be null.");
        }
        if (resultText == null) {
            throw new IllegalArgumentException("Battle result text cannot be null.");
        }
    }
}