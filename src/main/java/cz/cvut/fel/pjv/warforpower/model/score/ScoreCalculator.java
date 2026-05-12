package cz.cvut.fel.pjv.warforpower.model.score;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.tiles.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Calculates final game score of all players based on owned bases
 * and owned tiles connected to at least one of their bases.
 */
public class ScoreCalculator {
    private static final int POINTS_PER_BASE = 2;
    private static final int POINTS_PER_CONNECTED_TILE = 1;

    /**
     * Calculates final score results for all players and determines the winner.
     *
     * @param gameMap game map
     * @param players players participating in the game
     * @return final game score result
     * @throws IllegalArgumentException if the map or players are invalid
     */
    public GameScoreResult calculateGameResult(GameMap gameMap, Player[] players) {
        if (gameMap == null) {
            throw new IllegalArgumentException("Game map cannot be null.");
        }
        if (players == null || players.length == 0) {
            throw new IllegalArgumentException("Players cannot be null or empty.");
        }

        List<ScoreResult> playerScores = new ArrayList<>();

        for (Player player : players) {
            int basePoints = countBasePoints(player);
            int connectedTilePoints = countConnectedTilePoints(gameMap, player);

            playerScores.add(new ScoreResult(player, basePoints, connectedTilePoints));
        }

        List<Player> winners = resolveWinners(playerScores);

        return new GameScoreResult(playerScores, winners);
    }

    /**
     * Calculates score points gained from bases controlled by the player.
     *
     * @param player player whose base score is evaluated
     * @return points from owned bases
     */
    private int countBasePoints(Player player) {
        return player.getBasesCount() * POINTS_PER_BASE;
    }

    /**
     * Calculates score points for tiles owned by the player and connected
     * to at least one of their bases.
     *
     * @param gameMap game map
     * @param player player whose connected territory is evaluated
     * @return points from connected owned tiles
     */
    private int countConnectedTilePoints(GameMap gameMap, Player player) {
        // Using multi-source BFS(Breadth-First Search) algorithm

        List<List<Boolean>> markedTiles = new ArrayList<>();
        for (int i = 0; i < GameMap.ROWS_NUMBER; i++) {
            List<Boolean> markedTilesRow = new ArrayList<>();
            for (int j = 0; j < GameMap.ROW_LENGTHS[i]; j++) {
                markedTilesRow.add(false);
            }

            markedTiles.add(markedTilesRow);
        }

        int result = 0;
        Queue<HexTileCoords> queue = new ArrayDeque<>();

        for (BaseTile base : gameMap.getBasesOfPlayer(player)) {
            HexTileCoords baseCoords = base.getTileCoords();

            queue.offer(baseCoords);
            markedTiles.get(baseCoords.rowIndex())
                    .set(baseCoords.tileIndex(), true);
        }

        while (!queue.isEmpty()) {
            HexTileCoords currentTileCoords = queue.poll();
            if (!(gameMap.getTile(currentTileCoords) instanceof BaseTile)) {
                result += POINTS_PER_CONNECTED_TILE;
            }

            for (OccupiableTile neighbourTile : gameMap.getNeighbourOccupiableTiles(currentTileCoords)) {
                HexTileCoords neighbourCoords = neighbourTile.getTileCoords();

                if (markedTiles.get(neighbourCoords.rowIndex()).get(neighbourCoords.tileIndex())) {
                    continue;
                }
                if (neighbourTile instanceof Ownable ownableTile && ownableTile.getOwner() == player) {
                    queue.offer(neighbourCoords);
                    markedTiles.get(neighbourCoords.rowIndex()).set(neighbourCoords.tileIndex(), true);
                }
            }
        }

        return result;
    }

    /**
     * Determines the player or players with the highest total score.
     *
     * @param playerScores score results of all players
     * @return list of winners; multiple players may win in case of a tie
     */
    private List<Player> resolveWinners(List<ScoreResult> playerScores) {
        List<Player> winners = new ArrayList<>();
        int bestScore = 0;

        for (ScoreResult scoreResult : playerScores) {
            int total = scoreResult.getTotalPoints();

            if (total > bestScore) {
                winners.clear();
                winners.add(scoreResult.player());
                bestScore = total;
            } else if (total == bestScore) {
                winners.add(scoreResult.player());
            }
        }

        return winners;
    }
}