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
    private static final int CITY_DICE_COUNT = 2;

    /**
     * Resolves a battle between attacking and defending player units.
     * If the first attempt ends in a draw, one reroll attempt is resolved.
     *
     * @param attackingUnits attacking units
     * @param defendingUnits defending units
     * @param tileOfBattle tile on which the battle takes place
     * @return resolved battle result
     */
    public BattleResult resolvePlayerVsPlayer(
            List<Unit> attackingUnits,
            List<Unit> defendingUnits,
            OccupiableTile tileOfBattle
    ) {
        validatePlayerVsPlayerBattle(attackingUnits, defendingUnits, tileOfBattle);

        BattleAttemptResult firstAttempt = resolvePlayerVsPlayerAttempt(
                attackingUnits,
                defendingUnits,
                tileOfBattle
        );

        if (!firstAttempt.isDraw()) {
            return new BattleResult(
                    tileOfBattle,
                    firstAttempt,
                    null,
                    firstAttempt.battleOutcome()
            );
        }

        BattleAttemptResult secondAttempt = resolvePlayerVsPlayerAttempt(
                attackingUnits,
                defendingUnits,
                tileOfBattle
        );

        return new BattleResult(
                tileOfBattle,
                firstAttempt,
                secondAttempt,
                secondAttempt.battleOutcome()
        );
    }

    /**
     * Resolves a battle between player units and a city.
     * If the first attempt ends in a draw, one reroll attempt is resolved.
     *
     * @param attackingUnits attacking player units
     * @param cityTile defending city tile
     * @return resolved battle result
     */
    public BattleResult resolvePlayerVsCity(List<Unit> attackingUnits, CityTile cityTile) {
        validatePlayerVsCityBattle(attackingUnits, cityTile);

        BattleAttemptResult firstAttempt = resolvePlayerVsCityAttempt(attackingUnits, cityTile);

        if (!firstAttempt.isDraw()) {
            return new BattleResult(
                    cityTile,
                    firstAttempt,
                    null,
                    firstAttempt.battleOutcome()
            );
        }

        BattleAttemptResult secondAttempt = resolvePlayerVsCityAttempt(attackingUnits, cityTile);

        return new BattleResult(
                cityTile,
                firstAttempt,
                secondAttempt,
                secondAttempt.battleOutcome()
        );
    }

    /**
     * Resolves one battle attempt between two player-controlled sides.
     *
     * @param attackingUnits attacking units
     * @param defendingUnits defending units
     * @param tileOfBattle tile on which the battle takes place
     * @return resolved result of one battle attempt
     */
    private BattleAttemptResult resolvePlayerVsPlayerAttempt(
            List<Unit> attackingUnits,
            List<Unit> defendingUnits,
            OccupiableTile tileOfBattle
    ) {
        DiceRoll attackerRolls = rollDice(getDiceCountForUnits(attackingUnits));
        DiceRoll defenderRolls = rollDice(getDiceCountForUnits(defendingUnits));

        int attackerBonus = countBonusPoints(attackingUnits, tileOfBattle);
        int defenderBonus = countBonusPoints(defendingUnits, tileOfBattle);

        int attackerTotal = attackerRolls.getSum() + attackerBonus;
        int defenderTotal = defenderRolls.getSum() + defenderBonus;

        BattleOutcome battleOutcome = resolveOutcome(attackerTotal, defenderTotal);

        List<Unit> attackerLostUnits = resolveLostUnits(
                attackingUnits,
                attackerTotal,
                defenderTotal,
                battleOutcome,
                true,
                tileOfBattle
        );

        List<Unit> defenderLostUnits = resolveLostUnits(
                defendingUnits,
                defenderTotal,
                attackerTotal,
                battleOutcome,
                false,
                tileOfBattle
        );

        BattleSideResult attackerResult = new BattleSideResult(
                attackingUnits,
                attackerRolls,
                attackerBonus,
                attackerLostUnits
        );

        BattleSideResult defenderResult = new BattleSideResult(
                defendingUnits,
                defenderRolls,
                defenderBonus,
                defenderLostUnits
        );

        return new BattleAttemptResult(attackerResult, defenderResult, battleOutcome);
    }

    /**
     * Resolves one battle attempt between player units and a city.
     *
     * @param attackingUnits attacking units
     * @param cityTile defending city
     * @return resolved result of one battle attempt
     */
    private BattleAttemptResult resolvePlayerVsCityAttempt(List<Unit> attackingUnits, CityTile cityTile) {
        DiceRoll attackerRolls = rollDice(getDiceCountForUnits(attackingUnits));
        DiceRoll defenderRolls = rollDice(CITY_DICE_COUNT);

        int attackerBonus = 0;
        int defenderBonus = 0;

        int attackerTotal = attackerRolls.getSum() + attackerBonus;
        int defenderTotal = defenderRolls.getSum() + defenderBonus;

        BattleOutcome battleOutcome = resolveOutcome(attackerTotal, defenderTotal);

        List<Unit> attackerLostUnits = resolveLostUnitsAgainstCity(
                attackingUnits,
                attackerTotal,
                defenderTotal,
                battleOutcome
        );

        BattleSideResult attackerResult = new BattleSideResult(
                attackingUnits,
                attackerRolls,
                attackerBonus,
                attackerLostUnits
        );

        BattleSideResult defenderResult = new BattleSideResult(
                List.of(),
                defenderRolls,
                defenderBonus,
                List.of()
        );

        return new BattleAttemptResult(attackerResult, defenderResult, battleOutcome);
    }

    /**
     * Rolls the specified number of dice.
     * Can be overridden in tests to provide deterministic rolls.
     *
     * @param diceCount number of dice to roll
     * @return rolled dice values
     */
    protected DiceRoll rollDice(int diceCount) {
        return Dice.rollDice(diceCount);
    }

    /**
     * Validates a player-vs-player battle before resolution.
     *
     * @param attackingUnits attacking units
     * @param defendingUnits defending units
     * @param tileOfBattle tile of battle
     */
    private void validatePlayerVsPlayerBattle(
            List<Unit> attackingUnits,
            List<Unit> defendingUnits,
            OccupiableTile tileOfBattle
    ) {
        if (attackingUnits == null || attackingUnits.isEmpty()) {
            throw new IllegalArgumentException("Attacking units cannot be null or empty.");
        }
        if (defendingUnits == null || defendingUnits.isEmpty()) {
            throw new IllegalArgumentException("Defending units cannot be null or empty.");
        }
        if (tileOfBattle == null) {
            throw new IllegalArgumentException("Tile of battle cannot be null.");
        }
        if (attackingUnits.size() > 2 || defendingUnits.size() > 2) {
            throw new IllegalArgumentException("Battle supports at most two units per side.");
        }
    }

    /**
     * Validates a player-vs-city battle before resolution.
     *
     * @param attackingUnits attacking units
     * @param cityTile defending city
     */
    private void validatePlayerVsCityBattle(List<Unit> attackingUnits, CityTile cityTile) {
        if (attackingUnits == null || attackingUnits.isEmpty()) {
            throw new IllegalArgumentException("Attacking units cannot be null or empty.");
        }
        if (cityTile == null) {
            throw new IllegalArgumentException("City tile cannot be null.");
        }
        if (attackingUnits.size() > 2) {
            throw new IllegalArgumentException("Battle supports at most two attacking units.");
        }
    }

    /**
     * Returns the number of dice rolled by the specified unit group.
     *
     * One unit rolls two dice, two units roll four dice.
     *
     * @param units participating units
     * @return number of dice to roll
     */
    private int getDiceCountForUnits(List<Unit> units) {
        if (units == null || units.isEmpty()) {
            throw new IllegalArgumentException("Units cannot be null or empty.");
        }
        if (units.size() == 1) {
            return 2;
        }
        if (units.size() == 2) {
            return 4;
        }
        throw new IllegalArgumentException("Only 1 or 2 units are supported.");
    }

    /**
     * Counts total terrain bonus points of the specified units on the given tile.
     *
     * Non-terrain tiles provide no terrain bonuses.
     *
     * @param units participating units
     * @param tileOfBattle tile on which the battle takes place
     * @return total bonus points
     */
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

    /**
     * Resolves battle outcome from total points of both sides.
     *
     * @param attackerTotal attacker total points
     * @param defenderTotal defender total points
     * @return resolved battle outcome
     */
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
     * Resolves units lost by the attacking side in a battle against a city.
     *
     * @param attackingUnits attacking units
     * @param attackerTotal attacker total points
     * @param defenderTotal city total points
     * @param battleOutcome battle outcome
     * @return lost attacking units
     */
    private List<Unit> resolveLostUnitsAgainstCity(
            List<Unit> attackingUnits,
            int attackerTotal,
            int defenderTotal,
            BattleOutcome battleOutcome
    ) {
        List<Unit> lostUnits = new ArrayList<>();

        if (battleOutcome == BattleOutcome.DRAW) {
            return lostUnits;
        }

        if (battleOutcome == BattleOutcome.DEFENDER_WIN) {
            lostUnits.addAll(attackingUnits);
            return lostUnits;
        }

        if (attackingUnits.size() == 1) {
            return lostUnits;
        }

        int pointDifference = Math.abs(attackerTotal - defenderTotal);
        if (pointDifference <= 2) {
            lostUnits.add(chooseRandomUnit(attackingUnits));
        }

        return lostUnits;
    }

    /**
     * Resolves which winning unit is lost after a close victory.
     *
     * If the battle tile provides terrain modifiers, the unit with the lower
     * terrain modifier is lost first. If both units have the same modifier,
     * one unit is chosen randomly.
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

    /**
     * Chooses one random unit from the specified list.
     *
     * @param units available units
     * @return randomly chosen unit
     */
    private Unit chooseRandomUnit(List<Unit> units) {
        int randomIndex = ThreadLocalRandom.current().nextInt(units.size());
        return units.get(randomIndex);
    }
}