package cz.cvut.fel.pjv.warforpower.model.tiles;

import cz.cvut.fel.pjv.warforpower.model.players.Player;

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

    public boolean isUnitBoughtThisRound() {
        return unitBoughtThisRound;
    }

    public void markUnitBoughtThisRound() {
        if (unitBoughtThisRound) {
            throw new IllegalStateException("A unit has already been bought on this base this round.");
        }
        unitBoughtThisRound = true;
    }

    public void resetRoundPurchaseState() {
        unitBoughtThisRound = false;
    }
}
