package cz.cvut.fel.pjv.warforpower.model.players;

import cz.cvut.fel.pjv.warforpower.model.game.Game;

import java.util.Arrays;
import java.util.Collections;

public final class PlayersFactory {
    private PlayersFactory() {}

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
