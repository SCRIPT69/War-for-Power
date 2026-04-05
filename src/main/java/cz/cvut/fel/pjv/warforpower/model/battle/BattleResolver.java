package cz.cvut.fel.pjv.warforpower.model.battle;

import cz.cvut.fel.pjv.warforpower.model.tiles.CityTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Resolves battle outcomes between attacking and defending sides.
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
    public BattleResult resolvePlayerVsPlayer(
            List<Unit> attackerUnits,
            List<Unit> defenderUnits,
            OccupiableTile tileOfBattle
    ) {
        validatePlayerVsPlayerBattle(attackerUnits, defenderUnits, tileOfBattle);

        DiceRoll attackerRolls = Dice.getTwoDiceResults();
        DiceRoll defenderRolls = Dice.getTwoDiceResults();

        int attackerBonus = countBonusPoints(attackerUnits, tileOfBattle);
        int defenderBonus = countBonusPoints(defenderUnits, tileOfBattle);

        int attackerTotal = attackerRolls.getSum() + attackerBonus;
        int defenderTotal = defenderRolls.getSum() + defenderBonus;

        BattleOutcome battleOutcome = resolveOutcome(attackerTotal, defenderTotal);

        List<Unit> attackerLostUnits = resolveLostUnits(
                attackerUnits,
                attackerTotal,
                defenderTotal,
                battleOutcome,
                true,
                tileOfBattle
        );

        List<Unit> defenderLostUnits = resolveLostUnits(
                defenderUnits,
                defenderTotal,
                attackerTotal,
                battleOutcome,
                false,
                tileOfBattle
        );

        BattleSideResult attackerResult = new BattleSideResult(
                attackerUnits,
                attackerRolls,
                attackerBonus,
                attackerLostUnits
        );

        BattleSideResult defenderResult = new BattleSideResult(
                defenderUnits,
                defenderRolls,
                defenderBonus,
                defenderLostUnits
        );

        return new BattleResult(tileOfBattle, attackerResult, defenderResult, battleOutcome);
    }

    /**
     * Resolves a battle between player units and a city.
     *
     * @param playerUnits attacking player units
     * @param cityTile defending city tile
     * @return resolved battle result
     */
    public BattleResult resolvePlayerVsCity(List<Unit> playerUnits, CityTile cityTile) {
        // TODO: define exact city defense rules
        return null;
    }

    private void validatePlayerVsPlayerBattle(
            List<Unit> attackerUnits,
            List<Unit> defenderUnits,
            OccupiableTile tileOfBattle
    ) {
        if (attackerUnits == null || attackerUnits.isEmpty()) {
            throw new IllegalArgumentException("Attacker units cannot be null or empty.");
        }
        if (defenderUnits == null || defenderUnits.isEmpty()) {
            throw new IllegalArgumentException("Defender units cannot be null or empty.");
        }
        if (tileOfBattle == null) {
            throw new IllegalArgumentException("Tile of battle cannot be null.");
        }
        // TODO: maybe some more validation rules
    }

    private int countBonusPoints(List<Unit> units, OccupiableTile tileOfBattle) {
        if (!(tileOfBattle instanceof TerrainTile terrainTile)) {
            return 0;
        }

        int bonus = 0;
        for (Unit unit : units) {
            bonus += unit.getTerrainModifier(terrainTile.getTerrainType());
        }

        return bonus;
    }

    private BattleOutcome resolveOutcome(int attackerTotal, int defenderTotal) {
        if (attackerTotal > defenderTotal) {
            return BattleOutcome.ATTACKER_WIN;
        }
        if (defenderTotal > attackerTotal) {
            return BattleOutcome.DEFENDER_WIN;
        }
        return BattleOutcome.DRAW;
    }

    /**
     * Resolves units lost by one battle side according to battle rules.
     *
     * Losing side always loses all participating units.
     * Winning side:
     * - loses no units if only one unit participated
     * - loses no units if two units participated and point difference is greater than 2
     * - loses one unit if two units participated and point difference is 2 or less
     *
     * @param sideUnits units of the evaluated side
     * @param sideTotal total points of the evaluated side
     * @param opposingTotal total points of the opposing side
     * @param battleOutcome resolved battle outcome
     * @param attackerSide true if the evaluated side is the attacker
     * @param tileOfBattle tile on which the battle takes place
     * @return list of units lost by the evaluated side
     */
    private List<Unit> resolveLostUnits(
            List<Unit> sideUnits,
            int sideTotal,
            int opposingTotal,
            BattleOutcome battleOutcome,
            boolean attackerSide,
            OccupiableTile tileOfBattle
    ) {
        List<Unit> lostUnits = new ArrayList<>();

        boolean sideWon =
                (attackerSide && battleOutcome == BattleOutcome.ATTACKER_WIN) ||
                        (!attackerSide && battleOutcome == BattleOutcome.DEFENDER_WIN);

        boolean sideLost =
                (attackerSide && battleOutcome == BattleOutcome.DEFENDER_WIN) ||
                        (!attackerSide && battleOutcome == BattleOutcome.ATTACKER_WIN);

        if (battleOutcome == BattleOutcome.DRAW) {
            return lostUnits;
        }

        if (sideLost) {
            lostUnits.addAll(sideUnits);
            return lostUnits;
        }

        if (sideWon) {
            if (sideUnits.size() == 1) {
                return lostUnits;
            }

            int pointDifference = Math.abs(sideTotal - opposingTotal);
            if (pointDifference <= 2) {
                lostUnits.add(resolveLostWinningUnit(sideUnits, tileOfBattle));
            }
        }

        return lostUnits;
    }

    /**
     * Resolves which winning unit is lost after a close victory.
     *
     * Priority:
     * - unit with terrain disadvantage loses first
     * - if the other unit has terrain advantage, neutral unit loses
     * - if both units are equal relative to the terrain, one is chosen randomly
     *
     * @param winningUnits winning side units
     * @param tileOfBattle tile on which the battle takes place
     * @return lost winning unit
     */
    private Unit resolveLostWinningUnit(List<Unit> winningUnits, OccupiableTile tileOfBattle) {
        if (winningUnits == null || winningUnits.size() != 2) {
            throw new IllegalArgumentException("Winning side must contain exactly two units.");
        }

        if (!(tileOfBattle instanceof TerrainTile terrainTile)) {
            return chooseRandomUnit(winningUnits);
        }

        Unit firstUnit = winningUnits.get(0);
        Unit secondUnit = winningUnits.get(1);

        int firstModifier = firstUnit.getTerrainModifier(terrainTile.getTerrainType());
        int secondModifier = secondUnit.getTerrainModifier(terrainTile.getTerrainType());

        if (firstModifier < secondModifier) {
            return firstUnit;
        }
        if (secondModifier < firstModifier) {
            return secondUnit;
        }

        return chooseRandomUnit(winningUnits);
    }

    private Unit chooseRandomUnit(List<Unit> units) {
        int randomIndex = ThreadLocalRandom.current().nextInt(units.size());
        return units.get(randomIndex);
    }
}