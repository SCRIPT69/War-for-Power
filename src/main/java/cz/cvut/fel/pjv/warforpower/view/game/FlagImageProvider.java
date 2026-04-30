package cz.cvut.fel.pjv.warforpower.view.game;

import cz.cvut.fel.pjv.warforpower.model.players.PlayerColor;
import javafx.scene.image.Image;

import java.util.EnumMap;
import java.util.Map;

/**
 * Provides owner flag images for terrain tiles.
 */
public class FlagImageProvider {
    private final Map<PlayerColor, Image> flags = new EnumMap<>(PlayerColor.class);

    public FlagImageProvider() {
        flags.put(PlayerColor.RED, load("/img/flags/red_flag.png"));
        flags.put(PlayerColor.BLUE, load("/img/flags/blue_flag.png"));
        flags.put(PlayerColor.LIGHTBLUE, load("/img/flags/lightblue_flag.png"));
        flags.put(PlayerColor.PURPLE, load("/img/flags/purple_flag.png"));
    }

    public Image getFlagImage(PlayerColor color) {
        return flags.get(color);
    }

    private Image load(String path) {
        return new Image(getClass().getResource(path).toExternalForm());
    }
}