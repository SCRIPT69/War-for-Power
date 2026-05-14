package cz.cvut.fel.pjv.warforpower.model.game;

import cz.cvut.fel.pjv.warforpower.model.battle.BattleAttemptResult;
import cz.cvut.fel.pjv.warforpower.model.battle.BattleOutcome;
import cz.cvut.fel.pjv.warforpower.model.battle.BattleResult;
import cz.cvut.fel.pjv.warforpower.model.battle.BattleSideResult;
import cz.cvut.fel.pjv.warforpower.model.battle.DiceRoll;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.CityTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for applying resolved battle results to the game state.
 */
class GameApplyBattleResultTest {
    private Game game;
    private Player attackerPlayer;
    private Player defenderPlayer;
    private BaseTile attackerBase;
    private BaseTile defenderBase;

    @BeforeEach
    void setUp() {
        game = new Game(2);
        game.startNewGame();

        attackerPlayer = game.getPlayers()[0];
        defenderPlayer = game.getPlayers()[1];

        attackerBase = game.getGameMap().getBasesOfPlayer(attackerPlayer).getFirst();
        defenderBase = game.getGameMap().getBasesOfPlayer(defenderPlayer).getFirst();
    }

    @Test
    void applyBattleResult_attackerWin_movesSurvivingAttackerToBattleTile() {
        Unit attackerUnit = createUnitOnTile(UnitType.INFANTRY, attackerPlayer, attackerBase);
        Unit defenderUnit = createUnitOnTile(UnitType.INFANTRY, defenderPlayer, defenderBase);

        BattleResult battleResult = createBattleResult(
                defenderBase,
                List.of(attackerUnit),
                List.of(defenderUnit),
                List.of(),
                List.of(defenderUnit),
                BattleOutcome.ATTACKER_WIN
        );

        game.applyBattleResult(battleResult);

        assertEquals(defenderBase, attackerUnit.getOccupiedTile());
        assertTrue(defenderBase.getStandingUnits().contains(attackerUnit));
        assertFalse(attackerBase.getStandingUnits().contains(attackerUnit));
        assertFalse(defenderBase.getStandingUnits().contains(defenderUnit));
    }

    @Test
    void applyBattleResult_defenderWin_removesAttackingUnits() {
        Unit attackerUnit = createUnitOnTile(UnitType.INFANTRY, attackerPlayer, attackerBase);
        Unit defenderUnit = createUnitOnTile(UnitType.INFANTRY, defenderPlayer, defenderBase);

        BattleResult battleResult = createBattleResult(
                defenderBase,
                List.of(attackerUnit),
                List.of(defenderUnit),
                List.of(attackerUnit),
                List.of(),
                BattleOutcome.DEFENDER_WIN
        );

        game.applyBattleResult(battleResult);

        assertFalse(attackerBase.getStandingUnits().contains(attackerUnit));
        assertTrue(defenderBase.getStandingUnits().contains(defenderUnit));
        assertEquals(defenderBase, defenderUnit.getOccupiedTile());
    }

    @Test
    void applyBattleResult_draw_keepsUnitsOnOriginalTiles() {
        Unit attackerUnit = createUnitOnTile(UnitType.INFANTRY, attackerPlayer, attackerBase);
        Unit defenderUnit = createUnitOnTile(UnitType.INFANTRY, defenderPlayer, defenderBase);

        BattleResult battleResult = createBattleResult(
                defenderBase,
                List.of(attackerUnit),
                List.of(defenderUnit),
                List.of(),
                List.of(),
                BattleOutcome.DRAW
        );

        game.applyBattleResult(battleResult);

        assertEquals(attackerBase, attackerUnit.getOccupiedTile());
        assertEquals(defenderBase, defenderUnit.getOccupiedTile());
        assertTrue(attackerBase.getStandingUnits().contains(attackerUnit));
        assertTrue(defenderBase.getStandingUnits().contains(defenderUnit));
    }

    @Test
    void applyBattleResult_cityCaptured_convertsCityToBaseAndMovesAttackerThere() {
        Unit attackerUnit = createUnitOnTile(UnitType.INFANTRY, attackerPlayer, attackerBase);
        HexTile cityTile = game.getGameMap().getTile(new HexTileCoords(4, 4));
        assertInstanceOf(CityTile.class, cityTile);

        BattleResult battleResult = createBattleResult(
                cityTile,
                List.of(attackerUnit),
                List.of(),
                List.of(),
                List.of(),
                BattleOutcome.ATTACKER_WIN
        );

        int attackerBasesBefore = attackerPlayer.getBasesCount();

        game.applyBattleResult(battleResult);

        HexTile convertedTile = game.getGameMap().getTile(new HexTileCoords(4, 4));
        assertInstanceOf(BaseTile.class, convertedTile);

        BaseTile capturedBase = (BaseTile) convertedTile;
        assertEquals(attackerPlayer, capturedBase.getOwner());
        assertEquals(attackerBasesBefore + 1, attackerPlayer.getBasesCount());
        assertEquals(capturedBase, attackerUnit.getOccupiedTile());
        assertTrue(capturedBase.getStandingUnits().contains(attackerUnit));
    }

    @Test
    void applyBattleResult_closeVictory_removesOneWinningAttackerAndMovesSurvivor() {
        Unit attackerUnitOne = createUnitOnTile(UnitType.INFANTRY, attackerPlayer, attackerBase);
        Unit attackerUnitTwo = createUnitOnTile(UnitType.CAVALRY, attackerPlayer, attackerBase);
        Unit defenderUnit = createUnitOnTile(UnitType.INFANTRY, defenderPlayer, defenderBase);

        BattleResult battleResult = createBattleResult(
                defenderBase,
                List.of(attackerUnitOne, attackerUnitTwo),
                List.of(defenderUnit),
                List.of(attackerUnitOne),
                List.of(defenderUnit),
                BattleOutcome.ATTACKER_WIN
        );

        game.applyBattleResult(battleResult);

        assertFalse(attackerBase.getStandingUnits().contains(attackerUnitOne));
        assertFalse(defenderBase.getStandingUnits().contains(defenderUnit));

        assertEquals(defenderBase, attackerUnitTwo.getOccupiedTile());
        assertTrue(defenderBase.getStandingUnits().contains(attackerUnitTwo));
    }

    private Unit createUnitOnTile(UnitType unitType, Player owner, OccupiableTile tile) {
        Unit unit = new Unit(unitType, owner, tile);
        tile.addUnit(unit);
        return unit;
    }

    private BattleResult createBattleResult(
            HexTile tileOfBattle,
            List<Unit> attackingUnits,
            List<Unit> defendingUnits,
            List<Unit> attackerLostUnits,
            List<Unit> defenderLostUnits,
            BattleOutcome finalOutcome
    ) {
        BattleSideResult attackerResult = new BattleSideResult(
                attackingUnits,
                new DiceRoll(List.of(3, 3)),
                0,
                attackerLostUnits
        );

        BattleSideResult defenderResult = new BattleSideResult(
                defendingUnits,
                new DiceRoll(List.of(2, 2)),
                0,
                defenderLostUnits
        );

        BattleAttemptResult firstAttempt = new BattleAttemptResult(
                attackerResult,
                defenderResult,
                finalOutcome
        );

        return new BattleResult(
                tileOfBattle,
                firstAttempt,
                null,
                finalOutcome
        );
    }
}