package cz.cvut.fel.pjv.warforpower.model.tiles;

import cz.cvut.fel.pjv.warforpower.model.players.Player;

/**
 * A special occupiable tile representing a player's base.
 * Bases can recruit new units and contribute to player income.
 */
public class BaseTile extends OccupiableTile implements Ownable {
    private Player owner;
    private boolean unitBoughtThisRound;

    public BaseTile(HexTileCoords tileCoords, Player owner) {
        super(tileCoords, HexTileType.BASE);
        this.owner = owner;
        this.unitBoughtThisRound = false;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Player owner) {
        this.owner = owner;
    }

    /**
     * Returns whether a unit has already been bought on this base in the current round.
     *
     * @return true if a unit has already been bought this round
     */
    public boolean isUnitBoughtThisRound() {
        return unitBoughtThisRound;
    }

    /**
     * Marks that a unit has been bought on this base in the current round.
     */
    public void markUnitBoughtThisRound() {
        if (unitBoughtThisRound) {
            throw new IllegalStateException("A unit has already been bought on this base this round.");
        }
        unitBoughtThisRound = true;
    }

    /**
     * Resets round-based recruitment state of this base.
     */
    public void resetRoundPurchaseState() {
        unitBoughtThisRound = false;
    }
}
