package cz.cvut.fel.pjv.warforpower.model.battle;

import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.players.PlayerColor;
import cz.cvut.fel.pjv.warforpower.model.tiles.CityTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainType;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BattleResolver combat resolution logic.
 */
class BattleResolverTest {

    @Test
    void resolvePlayerVsPlayer_attackerWinInFirstAttempt_hasNoSecondAttempt() {
        TestBattleResolver resolver = new TestBattleResolver()
                .addRoll(new DiceRoll(List.of(6, 6)))
                .addRoll(new DiceRoll(List.of(1, 1)));

        TestBattleContext context = createSingleVsSingleContext(TerrainType.PLAINS);

        BattleResult result = resolver.resolvePlayerVsPlayer(
                List.of(context.attackerUnit()),
                List.of(context.defenderUnit()),
                context.battleTile()
        );

        assertEquals(BattleOutcome.ATTACKER_WIN, result.finalOutcome());
        assertFalse(result.hasSecondAttempt());
        assertEquals(BattleOutcome.ATTACKER_WIN, result.firstAttempt().battleOutcome());
        assertEquals(List.of(context.defenderUnit()), result.firstAttempt().defenderResult().lostUnits());
        assertTrue(result.firstAttempt().attackerResult().lostUnits().isEmpty());
    }

    @Test
    void resolvePlayerVsPlayer_defenderWinInFirstAttempt_hasNoSecondAttempt() {
        TestBattleResolver resolver = new TestBattleResolver()
                .addRoll(new DiceRoll(List.of(1, 1)))
                .addRoll(new DiceRoll(List.of(6, 6)));

        TestBattleContext context = createSingleVsSingleContext(TerrainType.PLAINS);

        BattleResult result = resolver.resolvePlayerVsPlayer(
                List.of(context.attackerUnit()),
                List.of(context.defenderUnit()),
                context.battleTile()
        );

        assertEquals(BattleOutcome.DEFENDER_WIN, result.finalOutcome());
        assertFalse(result.hasSecondAttempt());
        assertEquals(BattleOutcome.DEFENDER_WIN, result.firstAttempt().battleOutcome());
        assertEquals(List.of(context.attackerUnit()), result.firstAttempt().attackerResult().lostUnits());
        assertTrue(result.firstAttempt().defenderResult().lostUnits().isEmpty());
    }

    @Test
    void resolvePlayerVsPlayer_drawInFirstAttempt_createsSecondAttempt() {
        TestBattleResolver resolver = new TestBattleResolver()
                .addRoll(new DiceRoll(List.of(3, 3)))
                .addRoll(new DiceRoll(List.of(3, 3)))
                .addRoll(new DiceRoll(List.of(6, 6)))
                .addRoll(new DiceRoll(List.of(1, 1)));

        TestBattleContext context = createSingleVsSingleContext(TerrainType.PLAINS);

        BattleResult result = resolver.resolvePlayerVsPlayer(
                List.of(context.attackerUnit()),
                List.of(context.defenderUnit()),
                context.battleTile()
        );

        assertTrue(result.hasSecondAttempt());
        assertEquals(BattleOutcome.DRAW, result.firstAttempt().battleOutcome());
        assertEquals(BattleOutcome.ATTACKER_WIN, result.secondAttempt().battleOutcome());
        assertEquals(BattleOutcome.ATTACKER_WIN, result.finalOutcome());
    }

    @Test
    void resolvePlayerVsPlayer_doubleDraw_finalOutcomeIsDraw() {
        TestBattleResolver resolver = new TestBattleResolver()
                .addRoll(new DiceRoll(List.of(4, 2)))
                .addRoll(new DiceRoll(List.of(3, 3)))
                .addRoll(new DiceRoll(List.of(5, 1)))
                .addRoll(new DiceRoll(List.of(2, 4)));

        TestBattleContext context = createSingleVsSingleContext(TerrainType.PLAINS);

        BattleResult result = resolver.resolvePlayerVsPlayer(
                List.of(context.attackerUnit()),
                List.of(context.defenderUnit()),
                context.battleTile()
        );

        assertTrue(result.hasSecondAttempt());
        assertEquals(BattleOutcome.DRAW, result.firstAttempt().battleOutcome());
        assertEquals(BattleOutcome.DRAW, result.secondAttempt().battleOutcome());
        assertEquals(BattleOutcome.DRAW, result.finalOutcome());

        assertTrue(result.firstAttempt().attackerResult().lostUnits().isEmpty());
        assertTrue(result.firstAttempt().defenderResult().lostUnits().isEmpty());
        assertTrue(result.secondAttempt().attackerResult().lostUnits().isEmpty());
        assertTrue(result.secondAttempt().defenderResult().lostUnits().isEmpty());
    }

    @Test
    void resolvePlayerVsPlayer_closeAttackerVictoryWithTwoUnits_losesOneWinningUnit() {
        Player attacker = new Player("Attacker", PlayerColor.RED, 500);
        Player defender = new Player("Defender", PlayerColor.BLUE, 500);
        TerrainType terrainType = TerrainType.PLAINS;
        OccupiableTile battleTile = new TerrainTile(new HexTileCoords(4, 4), terrainType);

        Unit attackerUnitOne = new Unit(UnitType.INFANTRY, attacker, battleTile);
        Unit attackerUnitTwo = new Unit(UnitType.CAVALRY, attacker, battleTile);
        Unit defenderUnit = new Unit(UnitType.INFANTRY, defender, battleTile);

        int attackerBonus = countBonusPoints(List.of(attackerUnitOne, attackerUnitTwo), terrainType);
        int defenderBonus = countBonusPoints(List.of(defenderUnit), terrainType);

        int defenderRollSum = 6;
        int attackerRollSum = defenderRollSum + defenderBonus - attackerBonus + 1;

        TestBattleResolver resolver = new TestBattleResolver()
                .addRoll(createRollWithSum(4, attackerRollSum))
                .addRoll(createRollWithSum(2, defenderRollSum));

        BattleResult result = resolver.resolvePlayerVsPlayer(
                List.of(attackerUnitOne, attackerUnitTwo),
                List.of(defenderUnit),
                battleTile
        );

        BattleAttemptResult firstAttempt = result.firstAttempt();

        assertEquals(BattleOutcome.ATTACKER_WIN, result.finalOutcome());
        assertEquals(1, firstAttempt.attackerResult().lostUnits().size());
        assertTrue(firstAttempt.attackerResult().lostUnits().contains(attackerUnitOne)
                || firstAttempt.attackerResult().lostUnits().contains(attackerUnitTwo));
        assertEquals(List.of(defenderUnit), firstAttempt.defenderResult().lostUnits());
    }

    @Test
    void resolvePlayerVsPlayer_oneAttacker_rollsTwoDice() {
        TestBattleResolver resolver = new TestBattleResolver()
                .addRoll(new DiceRoll(List.of(2, 5)))
                .addRoll(new DiceRoll(List.of(1, 1)));

        TestBattleContext context = createSingleVsSingleContext(TerrainType.PLAINS);

        BattleResult result = resolver.resolvePlayerVsPlayer(
                List.of(context.attackerUnit()),
                List.of(context.defenderUnit()),
                context.battleTile()
        );

        assertEquals(2, result.firstAttempt().attackerResult().rolls().values().size());
    }

    @Test
    void resolvePlayerVsPlayer_twoAttackers_rollFourDice() {
        Player attacker = new Player("Attacker", PlayerColor.RED, 500);
        Player defender = new Player("Defender", PlayerColor.BLUE, 500);
        OccupiableTile battleTile = new TerrainTile(new HexTileCoords(4, 4), TerrainType.PLAINS);

        Unit attackerUnitOne = new Unit(UnitType.INFANTRY, attacker, battleTile);
        Unit attackerUnitTwo = new Unit(UnitType.CAVALRY, attacker, battleTile);
        Unit defenderUnit = new Unit(UnitType.INFANTRY, defender, battleTile);

        TestBattleResolver resolver = new TestBattleResolver()
                .addRoll(new DiceRoll(List.of(1, 2, 3, 4)))
                .addRoll(new DiceRoll(List.of(1, 1)));

        BattleResult result = resolver.resolvePlayerVsPlayer(
                List.of(attackerUnitOne, attackerUnitTwo),
                List.of(defenderUnit),
                battleTile
        );

        assertEquals(4, result.firstAttempt().attackerResult().rolls().values().size());
    }

    @Test
    void resolvePlayerVsPlayer_twoDefenders_rollFourDice() {
        Player attacker = new Player("Attacker", PlayerColor.RED, 500);
        Player defender = new Player("Defender", PlayerColor.BLUE, 500);
        OccupiableTile battleTile = new TerrainTile(new HexTileCoords(4, 4), TerrainType.PLAINS);

        Unit attackerUnit = new Unit(UnitType.INFANTRY, attacker, battleTile);
        Unit defenderUnitOne = new Unit(UnitType.INFANTRY, defender, battleTile);
        Unit defenderUnitTwo = new Unit(UnitType.CAVALRY, defender, battleTile);

        TestBattleResolver resolver = new TestBattleResolver()
                .addRoll(new DiceRoll(List.of(6, 6)))
                .addRoll(new DiceRoll(List.of(1, 2, 3, 4)))
                .addRoll(new DiceRoll(List.of(6, 6)))
                .addRoll(new DiceRoll(List.of(1, 1, 1, 1)));

        BattleResult result = resolver.resolvePlayerVsPlayer(
                List.of(attackerUnit),
                List.of(defenderUnitOne, defenderUnitTwo),
                battleTile
        );

        assertEquals(4, result.firstAttempt().defenderResult().rolls().values().size());
    }

    @Test
    void resolvePlayerVsCity_cityDefenderRollsTwoDiceAndHasNoUnits() {
        Player attacker = new Player("Attacker", PlayerColor.RED, 500);
        CityTile cityTile = new CityTile(new HexTileCoords(4, 4));
        Unit attackerUnit = new Unit(
                UnitType.INFANTRY,
                attacker,
                new TerrainTile(new HexTileCoords(4, 3), TerrainType.PLAINS)
        );

        TestBattleResolver resolver = new TestBattleResolver()
                .addRoll(new DiceRoll(List.of(6, 6)))
                .addRoll(new DiceRoll(List.of(1, 1)));

        BattleResult result = resolver.resolvePlayerVsCity(List.of(attackerUnit), cityTile);

        assertEquals(2, result.firstAttempt().defenderResult().rolls().values().size());
        assertTrue(result.firstAttempt().defenderResult().units().isEmpty());
    }

    @Test
    void resolvePlayerVsCity_defenderWin_attackerLosesAllUnits() {
        Player attacker = new Player("Attacker", PlayerColor.RED, 500);
        CityTile cityTile = new CityTile(new HexTileCoords(4, 4));
        Unit attackerUnit = new Unit(
                UnitType.INFANTRY,
                attacker,
                new TerrainTile(new HexTileCoords(4, 3), TerrainType.PLAINS)
        );

        TestBattleResolver resolver = new TestBattleResolver()
                .addRoll(new DiceRoll(List.of(1, 1)))
                .addRoll(new DiceRoll(List.of(6, 6)));

        BattleResult result = resolver.resolvePlayerVsCity(List.of(attackerUnit), cityTile);

        assertEquals(BattleOutcome.DEFENDER_WIN, result.finalOutcome());
        assertEquals(List.of(attackerUnit), result.firstAttempt().attackerResult().lostUnits());
    }

    @Test
    void resolvePlayerVsPlayer_secondAttemptOutcomeBecomesFinalOutcome() {
        TestBattleResolver resolver = new TestBattleResolver()
                .addRoll(new DiceRoll(List.of(3, 3)))
                .addRoll(new DiceRoll(List.of(3, 3)))
                .addRoll(new DiceRoll(List.of(1, 1)))
                .addRoll(new DiceRoll(List.of(6, 6)));

        TestBattleContext context = createSingleVsSingleContext(TerrainType.PLAINS);

        BattleResult result = resolver.resolvePlayerVsPlayer(
                List.of(context.attackerUnit()),
                List.of(context.defenderUnit()),
                context.battleTile()
        );

        assertTrue(result.hasSecondAttempt());
        assertEquals(result.secondAttempt().battleOutcome(), result.finalOutcome());
    }

    @Test
    void resolvePlayerVsPlayer_nullAttackers_throwsException() {
        TestBattleContext context = createSingleVsSingleContext(TerrainType.PLAINS);
        BattleResolver resolver = new BattleResolver();

        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolvePlayerVsPlayer(null, List.of(context.defenderUnit()), context.battleTile()));
    }

    @Test
    void resolvePlayerVsPlayer_emptyAttackers_throwsException() {
        TestBattleContext context = createSingleVsSingleContext(TerrainType.PLAINS);
        BattleResolver resolver = new BattleResolver();

        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolvePlayerVsPlayer(List.of(), List.of(context.defenderUnit()), context.battleTile()));
    }

    @Test
    void resolvePlayerVsPlayer_nullTile_throwsException() {
        TestBattleContext context = createSingleVsSingleContext(TerrainType.PLAINS);
        BattleResolver resolver = new BattleResolver();

        assertThrows(IllegalArgumentException.class,
                () -> resolver.resolvePlayerVsPlayer(
                        List.of(context.attackerUnit()),
                        List.of(context.defenderUnit()),
                        null
                ));
    }

    @Test
    void battleAttemptResult_isDraw_returnsTrueForDrawOutcome() {
        BattleAttemptResult drawResult = new BattleAttemptResult(
                new BattleSideResult(List.of(), new DiceRoll(List.of(3)), 0, List.of()),
                new BattleSideResult(List.of(), new DiceRoll(List.of(3)), 0, List.of()),
                BattleOutcome.DRAW
        );

        assertTrue(drawResult.isDraw());
    }

    @Test
    void battleAttemptResult_isDraw_returnsFalseForAttackerWin() {
        BattleAttemptResult result = new BattleAttemptResult(
                new BattleSideResult(List.of(), new DiceRoll(List.of(5)), 0, List.of()),
                new BattleSideResult(List.of(), new DiceRoll(List.of(3)), 0, List.of()),
                BattleOutcome.ATTACKER_WIN
        );

        assertFalse(result.isDraw());
    }

    private TestBattleContext createSingleVsSingleContext(TerrainType terrainType) {
        Player attacker = new Player("Attacker", PlayerColor.RED, 500);
        Player defender = new Player("Defender", PlayerColor.BLUE, 500);
        OccupiableTile battleTile = new TerrainTile(new HexTileCoords(4, 4), terrainType);

        Unit attackerUnit = new Unit(UnitType.INFANTRY, attacker, battleTile);
        Unit defenderUnit = new Unit(UnitType.INFANTRY, defender, battleTile);

        return new TestBattleContext(attacker, defender, battleTile, attackerUnit, defenderUnit);
    }

    private int countBonusPoints(List<Unit> units, TerrainType terrainType) {
        int bonus = 0;
        for (Unit unit : units) {
            bonus += unit.getTerrainModifier(terrainType);
        }
        return bonus;
    }

    private DiceRoll createRollWithSum(int diceCount, int targetSum) {
        int minSum = diceCount;
        int maxSum = diceCount * 6;

        if (targetSum < minSum || targetSum > maxSum) {
            throw new IllegalArgumentException(
                    "Target sum " + targetSum + " is not reachable with " + diceCount + " dice."
            );
        }

        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < diceCount; i++) {
            values.add(1);
        }

        int remaining = targetSum - minSum;
        for (int i = 0; i < diceCount && remaining > 0; i++) {
            int add = Math.min(5, remaining);
            values.set(i, values.get(i) + add);
            remaining -= add;
        }

        return new DiceRoll(values);
    }

    private record TestBattleContext(
            Player attacker,
            Player defender,
            OccupiableTile battleTile,
            Unit attackerUnit,
            Unit defenderUnit
    ) {
    }

    private static class TestBattleResolver extends BattleResolver {
        private final ArrayDeque<DiceRoll> queuedRolls = new ArrayDeque<>();

        TestBattleResolver addRoll(DiceRoll roll) {
            queuedRolls.add(roll);
            return this;
        }

        @Override
        protected DiceRoll rollDice(int diceCount) {
            if (queuedRolls.isEmpty()) {
                throw new IllegalStateException("No predefined dice roll available.");
            }

            DiceRoll roll = queuedRolls.remove();
            if (roll.values().size() != diceCount) {
                throw new IllegalStateException(
                        "Expected " + diceCount + " dice, but got " + roll.values().size() + "."
                );
            }

            return roll;
        }
    }
}