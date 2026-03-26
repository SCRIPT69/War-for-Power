package cz.cvut.fel.pjv.warforpower.model.players;

public class Player {
    private final String name;
    private final PlayerColor color;

    public Player(String name, PlayerColor color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public PlayerColor getColor() {
        return color;
    }
}
