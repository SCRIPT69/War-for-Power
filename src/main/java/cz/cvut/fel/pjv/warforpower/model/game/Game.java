package cz.cvut.fel.pjv.warforpower.model.game;

import cz.cvut.fel.pjv.warforpower.model.Player;

public class Game {
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;

    private final int playersNumber;
    private final Player[] players;

    public int getPlayersNumber() {
        return playersNumber;
    }

    public Game(int playersNumber) {
        if (playersNumber < MIN_PLAYERS || playersNumber > MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    "Players number must be between " + MIN_PLAYERS + " and " + MAX_PLAYERS + "."
            );
        }
        this.playersNumber = playersNumber;
        this.players = new Player[this.playersNumber];
    }

    public void startNewGame() {

    }

    private void generateMap() {

    }
}
