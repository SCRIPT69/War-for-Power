package cz.cvut.fel.pjv.warforpower.model.game;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.players.PlayersFactory;

public class Game {
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 4;

    private final int playersNumber;
    private final Player[] players;
    private final GameMap gameMap;

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
        this.players = PlayersFactory.createPlayers(playersNumber);
        this.gameMap = new GameMap();
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public void startNewGame() {
        gameMap.generateMap(players);
    }
}
