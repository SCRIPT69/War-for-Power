package cz.cvut.fel.pjv.warforpower.controller;

import cz.cvut.fel.pjv.warforpower.model.game.Game;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;

/**
 * Encapsulates rules deciding whether game objects are currently interactive.
 */
public class InteractionRules {
    private final Game game;

    public InteractionRules(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null.");
        }
        this.game = game;
    }

    /**
     * Returns whether the specified base is currently available for recruitment interaction.
     *
     * @param baseTile base tile to evaluate
     * @return true if the base is interactive for recruitment
     */
    public boolean isBaseInteractive(BaseTile baseTile) {
        if (baseTile == null) {
            throw new IllegalArgumentException("Base tile cannot be null.");
        }

        if (baseTile.getOwner() != game.getCurrentPlayer()) {
            return false;
        }
        if (baseTile.isUnitBoughtThisRound()) {
            return false;
        }
        if (baseTile.isFull()) {
            return false;
        }
        if (game.getGameMap().countUnitsOfPlayer(game.getCurrentPlayer()) >= game.getCurrentPlayer().getUnitsLimit()) {
            return false;
        }

        return game.getCurrentPlayer().getMoney() >= getCheapestUnitPrice();
    }

    /**
     * Returns whether the specified unit is currently interactive for selection.
     *
     * @param unit unit to evaluate
     * @return true if the unit can currently be selected
     */
    public boolean isUnitInteractive(Unit unit) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }

        return unit.getOwner() == game.getCurrentPlayer()
                && !unit.hasUsedMainActionThisRound();
    }

    private int getCheapestUnitPrice() {
        int cheapestPrice = Integer.MAX_VALUE;

        for (UnitType unitType : UnitType.values()) {
            cheapestPrice = Math.min(cheapestPrice, unitType.getPrice());
        }

        return cheapestPrice;
    }
}