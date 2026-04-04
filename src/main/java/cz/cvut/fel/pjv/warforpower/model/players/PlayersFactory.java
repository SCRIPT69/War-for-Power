package cz.cvut.fel.pjv.warforpower.model.players;

import cz.cvut.fel.pjv.warforpower.model.game.Game;

import java.util.Arrays;
import java.util.Collections;

/**
 * Factory class responsible for creating player instances
 * with default names, colors and starting resources.
 */
public final class PlayersFactory {
    private PlayersFactory() {}

    /**
     * Creates and returns an array of players for a new game.
     *
     * @param playersNumber number of players to create
     * @return created players
     * @throws IllegalStateException if there are not enough available colors
     */
    public static Player[] createPlayers(int playersNumber) {
        Player[] players = new Player[playersNumber];
        PlayerColor[] playerColors = PlayerColor.values();

        if (playersNumber > playerColors.length) {
            throw new IllegalStateException("Not enough colors for players.");
        }

        for (int i = 0; i < playersNumber; i++) {
            players[i] = new Player("Player " + (i+1), playerColors[i], Game.START_MONEY_AMOUNT);
        }

        //Randomly shuffling players order
        Collections.shuffle(Arrays.asList(players));

        return players;
    }
}
