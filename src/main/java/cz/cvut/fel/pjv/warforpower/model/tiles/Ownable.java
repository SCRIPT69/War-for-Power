package cz.cvut.fel.pjv.warforpower.model.tiles;

import cz.cvut.fel.pjv.warforpower.model.players.Player;


/**
 * Common interface for tiles that may be owned by a player.
 */
public interface Ownable {
    Player getOwner();

    void setOwner(Player owner);

    default boolean hasOwner() {
        return getOwner() != null;
    }
}