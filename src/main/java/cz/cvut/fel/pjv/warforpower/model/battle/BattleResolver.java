package cz.cvut.fel.pjv.warforpower.model.battle;

import cz.cvut.fel.pjv.warforpower.model.tiles.CityTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.List;

public class BattleResolver {
    public BattleResult resolvePlayerVsPlayer(List<Unit> attackerUnits, List<Unit> defenderUnits, OccupiableTile tileOfBattle) {
        // don't forget checking units if they already acted
        //don't forget unit.markActedThisRound();
        return null;
    }

    public BattleResult resolvePlayerVsCity(List<Unit> playerUnits, CityTile cityTile) {
        // don't forget checking units if they already acted
        //don't forget unit.markActedThisRound();
        return null;
    }
}