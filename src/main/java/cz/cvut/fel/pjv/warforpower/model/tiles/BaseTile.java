package cz.cvut.fel.pjv.warforpower.model.tiles;

import cz.cvut.fel.pjv.warforpower.model.players.Player;

/**
 * A special occupiable tile representing a player's base.
 * Bases can recruit new units and contribute to player income.
 */
public class BaseTile extends OccupiableTile implements Ownable {
    private Player owner;
    /**
     * True if a unit has already been bought on this base during the current round.
     * This prevents buying more than one unit from the same base in one round.
     */
    private boolean unitBoughtThisRound;
    /**
     * True if this base has been captured during the current round.
     * A captured base cannot be used to buy a unit until the next round.
     */
    private boolean capturedThisRound;

    public BaseTile(HexTileCoords tileCoords, Player owner) {
        super(tileCoords, HexTileType.BASE);
        this.owner = owner;
        this.unitBoughtThisRound = false;
        this.capturedThisRound = false;
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
     * Checks whether a unit can be bought on this base during the current round.
     *
     * A unit can be bought only if:
     * - no unit has been bought on this base this round;
     * - this base has not been captured this round.
     *
     * @return true if buying a unit is allowed, false otherwise
     */
    public boolean canBuyUnitThisRound() {
        return !unitBoughtThisRound && !capturedThisRound;
    }

    /**
     * Marks this base as captured during the current round.
     *
     * This method does not throw an exception if the base has already been captured
     * this round. Re-capturing the same base during one round is allowed by the
     * game flow, but the base must still remain unavailable for unit purchase until
     * the next round.
     */
    public void markCapturedThisRound() {
        capturedThisRound = true;
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
    public void resetRoundState() {
        unitBoughtThisRound = false;
        capturedThisRound = false;
    }

    /**
     * Returns whether a unit has been bought on this base during the current round.
     *
     * @return true if a unit was bought on this base this round
     */
    public boolean hasUnitBoughtThisRound() {
        return unitBoughtThisRound;
    }
    /**
     * Returns whether this base has been captured during the current round.
     *
     * @return true if this base was captured this round
     */
    public boolean hasBeenCapturedThisRound() {
        return capturedThisRound;
    }
    /**
     * Restores round-specific state of this base from a saved snapshot.
     *
     * This method should only be used during game loading, not during normal gameplay.
     *
     * @param unitBoughtThisRound saved unit-bought flag
     * @param capturedThisRound saved captured flag
     */
    public void restoreRoundState(boolean unitBoughtThisRound, boolean capturedThisRound) {
        this.unitBoughtThisRound = unitBoughtThisRound;
        this.capturedThisRound = capturedThisRound;
    }
}
