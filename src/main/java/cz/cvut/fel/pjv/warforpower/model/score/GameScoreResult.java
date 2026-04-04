package cz.cvut.fel.pjv.warforpower.model.score;

import cz.cvut.fel.pjv.warforpower.model.players.Player;

import java.util.List;

/**
 * Stores complete end-game scoring result including all player scores
 * and the list of winners.
 *
 * @param playerScores score results of all players
 * @param winners players with the highest total score
 */
public record GameScoreResult(
        List<ScoreResult> playerScores,
        List<Player> winners
) {
}