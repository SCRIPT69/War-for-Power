package cz.cvut.fel.pjv.warforpower.model.game;

import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.score.GameScoreResult;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for core Game rules such as game start, unit purchase,
 * movement, round progression and game ending.
 */
class GameTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game(2);
        game.startNewGame();
    }

    @Test
    void startNewGame_roundIsOne() {
        assertEquals(1, game.getCurrentRound());
    }

    @Test
    void startNewGame_eachPlayerHasOneBase() {
        for (Player player : game.getPlayers()) {
            assertEquals(1, player.getBasesCount());
        }
    }

    @Test
    void buyUnit_insufficientFunds_throwsException() {
        Player currentPlayer = game.getCurrentPlayer();
        currentPlayer.decreaseMoney(currentPlayer.getMoney());

        BaseTile baseTile = game.getGameMap().getBasesOfPlayer(currentPlayer).getFirst();

        assertThrows(IllegalStateException.class,
                () -> game.buyUnit(UnitType.INFANTRY, baseTile.getTileCoords()));
    }

    @Test
    void buyUnit_validPurchase_unitAppearsOnBase() {
        Player currentPlayer = game.getCurrentPlayer();
        BaseTile baseTile = game.getGameMap().getBasesOfPlayer(currentPlayer).getFirst();

        Unit boughtUnit = game.buyUnit(UnitType.INFANTRY, baseTile.getTileCoords());

        assertTrue(baseTile.hasUnits());
        assertTrue(baseTile.getStandingUnits().contains(boughtUnit));
    }

    @Test
    void buyUnit_validPurchase_moneyDecreased() {
        Player currentPlayer = game.getCurrentPlayer();
        int moneyBefore = currentPlayer.getMoney();
        BaseTile baseTile = game.getGameMap().getBasesOfPlayer(currentPlayer).getFirst();

        game.buyUnit(UnitType.INFANTRY, baseTile.getTileCoords());

        assertEquals(moneyBefore - UnitType.INFANTRY.getPrice(), currentPlayer.getMoney());
    }

    @Test
    void buyUnit_validPurchase_unitIsMarkedAsUsedThisRound() {
        Player currentPlayer = game.getCurrentPlayer();
        BaseTile baseTile = game.getGameMap().getBasesOfPlayer(currentPlayer).getFirst();

        Unit boughtUnit = game.buyUnit(UnitType.INFANTRY, baseTile.getTileCoords());

        assertTrue(boughtUnit.hasUsedMainActionThisRound());
    }

    @Test
    void buyUnit_twiceOnSameBase_throwsException() {
        Player currentPlayer = game.getCurrentPlayer();
        BaseTile baseTile = game.getGameMap().getBasesOfPlayer(currentPlayer).getFirst();

        game.buyUnit(UnitType.INFANTRY, baseTile.getTileCoords());

        assertThrows(IllegalStateException.class,
                () -> game.buyUnit(UnitType.ARCHERS, baseTile.getTileCoords()));
    }

    @Test
    void buyUnit_nullUnitType_throwsException() {
        Player currentPlayer = game.getCurrentPlayer();
        BaseTile baseTile = game.getGameMap().getBasesOfPlayer(currentPlayer).getFirst();

        assertThrows(IllegalArgumentException.class,
                () -> game.buyUnit(null, baseTile.getTileCoords()));
    }

    @Test
    void moveUnitToTile_validMove_unitChangesPosition() {
        Player currentPlayer = game.getCurrentPlayer();
        BaseTile baseTile = game.getGameMap().getBasesOfPlayer(currentPlayer).getFirst();

        Unit movableUnit = new Unit(UnitType.INFANTRY, currentPlayer, baseTile);
        baseTile.addUnit(movableUnit);

        List<OccupiableTile> movementOptions = game.getMovementOptions(movableUnit);
        assertFalse(movementOptions.isEmpty(), "Expected at least one movement option.");

        OccupiableTile targetTile = movementOptions.getFirst();
        game.moveUnitToTile(movableUnit, targetTile);

        assertEquals(targetTile, movableUnit.getOccupiedTile());
        assertFalse(baseTile.getStandingUnits().contains(movableUnit));
        assertTrue(targetTile.getStandingUnits().contains(movableUnit));
        assertTrue(movableUnit.hasUsedMainActionThisRound());
    }

    @Test
    void endTurn_switchesToNextPlayer() {
        Player firstPlayer = game.getCurrentPlayer();

        game.endTurn();

        assertNotEquals(firstPlayer, game.getCurrentPlayer());
    }

    @Test
    void endTurn_afterAllPlayers_roundIncreases() {
        int roundBefore = game.getCurrentRound();

        for (int i = 0; i < game.getPlayers().length; i++) {
            game.endTurn();
        }

        assertEquals(roundBefore + 1, game.getCurrentRound());
    }

    @Test
    void game_invalidPlayersCount_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> new Game(1));
        assertThrows(IllegalArgumentException.class, () -> new Game(5));
    }

    @Test
    void isGameEnded_initiallyFalse() {
        assertFalse(game.isGameEnded());
    }

    @Test
    void endGame_setsGameEnded() {
        game.endGame();

        assertTrue(game.isGameEnded());
    }

    @Test
    void endGame_twice_doesNotThrow() {
        game.endGame();

        assertDoesNotThrow(() -> game.endGame());
    }

    @Test
    void getFinalScoreResult_beforeGameEnd_throwsException() {
        assertThrows(IllegalStateException.class, () -> game.getFinalScoreResult());
    }

    @Test
    void getFinalScoreResult_afterGameEnd_returnsResult() {
        game.endGame();

        GameScoreResult result = game.getFinalScoreResult();

        assertNotNull(result);
    }
}