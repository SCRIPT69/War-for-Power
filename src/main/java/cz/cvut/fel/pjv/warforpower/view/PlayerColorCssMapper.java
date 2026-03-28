package cz.cvut.fel.pjv.warforpower.view;

import cz.cvut.fel.pjv.warforpower.model.players.PlayerColor;

public final class PlayerColorCssMapper {
    private PlayerColorCssMapper() {
    }

    public static String toCssColor(PlayerColor color) {
        return switch (color) {
            case RED -> "#ff0000";
            case BLUE -> "#0000ff";
            case LIGHTBLUE -> "#00eaff";
            case PURPLE -> "#ba00dc";
        };
    }
}
