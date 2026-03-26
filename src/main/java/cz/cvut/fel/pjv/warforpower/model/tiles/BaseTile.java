package cz.cvut.fel.pjv.warforpower.model.tiles;

import cz.cvut.fel.pjv.warforpower.model.players.Player;

public class BaseTile extends OccupiableTile implements Ownable {
    private Player owner;

    public BaseTile(HexTileCoords tileCoords, Player owner) {
        super(tileCoords, HexTileType.BASE);
        this.owner = owner;
    }

    @Override
    public Player getOwner() {
        return owner;
    }

    @Override
    public void setOwner(Player owner) {
        this.owner = owner;
    }
}
