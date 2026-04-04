package cz.cvut.fel.pjv.warforpower.model.score;

import cz.cvut.fel.pjv.warforpower.model.players.Player;

/**
 * Stores final score components of a single player.
 *
 * @param player scored player
 * @param basePoints points gained from owned bases
 * @param connectedTilePoints points gained from connected owned tiles
 */
public record ScoreResult(
        Player player,
        int basePoints,
        int connectedTilePoints
) {
    /**
     * Returns total score points of the player.
     *
     * @return total points
     */
    public int getTotalPoints() {
        return basePoints + connectedTilePoints;
    }
}