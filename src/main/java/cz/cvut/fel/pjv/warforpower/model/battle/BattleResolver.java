package cz.cvut.fel.pjv.warforpower.model.battle;

import cz.cvut.fel.pjv.warforpower.model.tiles.CityTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.List;

/**
 * Resolves battle outcomes between attacking and defending sides
 * and produces battle result objects for game logic and presentation.
 */
public class BattleResolver {
    /**
     * Resolves a battle between attacking and defending player units.
     *
     * @param attackerUnits attacking units
     * @param defenderUnits defending units
     * @param tileOfBattle tile on which the battle takes place
     * @return resolved battle result
     */
    public BattleResult resolvePlayerVsPlayer(List<Unit> attackerUnits, List<Unit> defenderUnits, OccupiableTile tileOfBattle) {
        // don't forget checking units if they already acted
        //don't forget unit.markActedThisRound();
        return null;
    }

    /**
     * Resolves a battle between player units and a city.
     *
     * @param playerUnits attacking player units
     * @param cityTile defending city tile
     * @return resolved battle result
     */
    public BattleResult resolvePlayerVsCity(List<Unit> playerUnits, CityTile cityTile) {
        // don't forget checking units if they already acted
        //don't forget unit.markActedThisRound();
        return null;
    }
}