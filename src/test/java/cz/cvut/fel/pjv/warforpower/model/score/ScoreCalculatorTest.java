package cz.cvut.fel.pjv.warforpower.model.score;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.players.PlayerColor;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainTile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ScoreCalculator base score, winner resolution
 * and connected territory scoring.
 */
class ScoreCalculatorTest {
    private ScoreCalculator calculator;
    private Player player;
    private Player enemy;
    private GameMap gameMap;

    @BeforeEach
    void setUp() {
        calculator = new ScoreCalculator();
        player = new Player("Test", PlayerColor.RED, 100);
        enemy = new Player("Enemy", PlayerColor.BLUE, 100);

        gameMap = new GameMap();
        gameMap.generateMap(new Player[]{player, enemy});
    }

    @Test
    void calculateGameResult_playerWithOnlyStartingBase_getsBasePointsOnly() {
        GameScoreResult result = calculator.calculateGameResult(gameMap, new Player[]{player, enemy});

        ScoreResult playerResult = findScoreResult(result, player);

        assertEquals(2, playerResult.basePoints());
        assertEquals(0, playerResult.connectedTilePoints());
        assertEquals(2, playerResult.getTotalPoints());
    }

    @Test
    void calculateGameResult_nullMap_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateGameResult(null, new Player[]{player}));
    }

    @Test
    void calculateGameResult_nullPlayers_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateGameResult(gameMap, null));
    }

    @Test
    void calculateGameResult_emptyPlayers_throwsException() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateGameResult(gameMap, new Player[0]));
    }

    @Test
    void calculateGameResult_playerWithOwnedTerrain_getsPositiveTerritoryPoints() {
        TerrainTile ownedTile = findNeighbourTerrainOfPlayerBase(gameMap, player);
        ownedTile.setOwner(player);

        GameScoreResult result = calculator.calculateGameResult(gameMap, new Player[]{player, enemy});
        ScoreResult playerResult = findScoreResult(result, player);

        assertTrue(playerResult.connectedTilePoints() > 0);
    }

    @Test
    void calculateGameResult_addingConnectedOwnedTile_doesNotDecreaseConnectedTilePoints() {
        TerrainTile firstOwnedTile = findNeighbourTerrainOfPlayerBase(gameMap, player);
        firstOwnedTile.setOwner(player);

        GameScoreResult oneTileResult = calculator.calculateGameResult(gameMap, new Player[]{player, enemy});
        int oneTileConnectedPoints = findScoreResult(oneTileResult, player).connectedTilePoints();

        TerrainTile secondConnectedTile = findNeighbourTerrainOfTile(gameMap, firstOwnedTile.getTileCoords());
        secondConnectedTile.setOwner(player);

        GameScoreResult twoTilesResult = calculator.calculateGameResult(gameMap, new Player[]{player, enemy});
        int twoTilesConnectedPoints = findScoreResult(twoTilesResult, player).connectedTilePoints();

        assertTrue(twoTilesConnectedPoints >= oneTileConnectedPoints);
    }

    @Test
    void calculateGameResult_winnerHasHigherScoreAndIsTheOnlyWinner() {
        TerrainTile ownedTile = findNeighbourTerrainOfPlayerBase(gameMap, player);
        ownedTile.setOwner(player);

        GameScoreResult result = calculator.calculateGameResult(gameMap, new Player[]{player, enemy});

        ScoreResult playerResult = findScoreResult(result, player);
        ScoreResult enemyResult = findScoreResult(result, enemy);

        assertTrue(playerResult.getTotalPoints() > enemyResult.getTotalPoints());
        assertTrue(result.winners().contains(player));
        assertFalse(result.winners().contains(enemy));
    }

    @Test
    void scoreResult_getTotalPoints_sumsBaseAndTerritoryPoints() {
        ScoreResult result = new ScoreResult(player, 4, 3);

        assertEquals(7, result.getTotalPoints());
    }

    @Test
    void calculateGameResult_twoPlayersEqualScore_bothAreWinners() {
        GameMap map = new GameMap();
        Player first = new Player("P1", PlayerColor.RED, 100);
        Player second = new Player("P2", PlayerColor.BLUE, 100);
        map.generateMap(new Player[]{first, second});

        GameScoreResult result = calculator.calculateGameResult(map, new Player[]{first, second});

        ScoreResult firstResult = findScoreResult(result, first);
        ScoreResult secondResult = findScoreResult(result, second);

        assertEquals(firstResult.getTotalPoints(), secondResult.getTotalPoints());
        assertEquals(2, result.winners().size());
        assertTrue(result.winners().contains(first));
        assertTrue(result.winners().contains(second));
    }

    private ScoreResult findScoreResult(GameScoreResult result, Player searchedPlayer) {
        return result.playerScores().stream()
                .filter(scoreResult -> scoreResult.player() == searchedPlayer)
                .findFirst()
                .orElseThrow();
    }

    private TerrainTile findNeighbourTerrainOfPlayerBase(GameMap map, Player owner) {
        BaseTile baseTile = map.getBasesOfPlayer(owner).getFirst();

        for (HexTile neighbour : map.getNeighbourTiles(baseTile.getTileCoords())) {
            if (neighbour instanceof TerrainTile terrainTile) {
                return terrainTile;
            }
        }

        throw new IllegalStateException("No neighbouring terrain tile found.");
    }

    private TerrainTile findNeighbourTerrainOfTile(GameMap map, HexTileCoords coords) {
        for (HexTile neighbour : map.getNeighbourTiles(coords)) {
            if (neighbour instanceof TerrainTile terrainTile) {
                return terrainTile;
            }
        }

        throw new IllegalStateException("No neighbouring terrain tile found.");
    }
}