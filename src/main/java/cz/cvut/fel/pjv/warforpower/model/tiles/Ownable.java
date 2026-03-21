package cz.cvut.fel.pjv.warforpower.model.tiles;

import cz.cvut.fel.pjv.warforpower.model.Player;

public interface Ownable {
    Player getOwner();

    void setOwner(Player owner);

    default boolean hasOwner() {
        return getOwner() != null;
    }
}