package cz.cvut.fel.pjv.warforpower.model.players;

public class Player {
    private final String name;
    private final PlayerColor color;
    private int money;
    private int basesCount = 0;
    private boolean eliminated = false;

    public Player(String name, PlayerColor color, int money) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Player name cannot be null or blank.");
        }
        if (color == null) {
            throw new IllegalArgumentException("Player color cannot be null.");
        }
        if (money < 0) {
            throw new IllegalArgumentException("Money cannot be negative.");
        }

        this.name = name;
        this.color = color;
        this.money = money;
    }

    public String getName() {
        return name;
    }

    public PlayerColor getColor() {
        return color;
    }

    public int getMoney() {
        return money;
    }

    public int getUnitsLimit() {
        return basesCount >= 2 ? 8 : 4;
    }

    public void increaseMoney(int amount) {
        if (amount < 1) {
            throw new IllegalStateException("The amount can not be less than 1.");
        }
        money += amount;
    }

    public void decreaseMoney(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("The amount can not be less than 1.");
        }
        if (amount > money) {
            throw new IllegalStateException("The amount can not be bigger than player's money.");
        }
        money -= amount;
    }

    public int getBasesCount() {
        return basesCount;
    }

    public void increaseBasesCount() {
        basesCount++;
    }

    public void decreaseBasesCount() {
        if (basesCount < 1) {
            throw new IllegalStateException("The player already has no bases.");
        }
        basesCount--;
    }

    public boolean isEliminated() {
        return eliminated;
    }

    public void setEliminated(boolean eliminated) {
        this.eliminated = eliminated;
    }
}
