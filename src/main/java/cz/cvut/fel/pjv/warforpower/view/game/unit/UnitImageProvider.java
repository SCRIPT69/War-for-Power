package cz.cvut.fel.pjv.warforpower.view.game.unit;

import cz.cvut.fel.pjv.warforpower.model.players.PlayerColor;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;
import javafx.scene.image.Image;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

/**
 * Provides images used for rendering units on the map.
 * All unit images are loaded once and then reused.
 */
public class UnitImageProvider {
    private final Map<UnitType, Map<PlayerColor, Image>> images =
            new EnumMap<>(UnitType.class);

    public UnitImageProvider() {
        for (UnitType unitType : UnitType.values()) {
            Map<PlayerColor, Image> imagesByColor = new EnumMap<>(PlayerColor.class);

            for (PlayerColor playerColor : PlayerColor.values()) {
                imagesByColor.put(playerColor, loadImage(unitType, playerColor));
            }

            images.put(unitType, imagesByColor);
        }
    }

    /**
     * Returns image corresponding to the specified unit type and owner color.
     *
     * @param unitType unit type
     * @param playerColor owner color
     * @return unit image
     */
    public Image getUnitImage(UnitType unitType, PlayerColor playerColor) {
        if (unitType == null) {
            throw new IllegalArgumentException("Unit type cannot be null.");
        }
        if (playerColor == null) {
            throw new IllegalArgumentException("Player color cannot be null.");
        }

        return images.get(unitType).get(playerColor);
    }

    /**
     * Loads a unit image for the specified unit type and player color.
     *
     * @param unitType unit type
     * @param playerColor player color
     * @return loaded image
     */
    private Image loadImage(UnitType unitType, PlayerColor playerColor) {
        String imageUrl = "/img/units/"
                + toColorName(playerColor)
                + "/"
                + toColorName(playerColor)
                + "_"
                + toUnitTypeName(unitType)
                + ".png";

        URL resource = getClass().getResource(imageUrl);
        if (resource == null) {
            throw new IllegalArgumentException("Unit image resource not found: " + imageUrl);
        }

        return new Image(resource.toExternalForm());
    }

    /**
     * Converts player color enum to corresponding resource folder/file name part.
     *
     * @param playerColor player color
     * @return lowercase color name used in resource paths
     */
    private String toColorName(PlayerColor playerColor) {
        return switch (playerColor) {
            case RED -> "red";
            case BLUE -> "blue";
            case LIGHTBLUE -> "lightblue";
            case PURPLE -> "purple";
        };
    }

    /**
     * Converts unit type enum to corresponding resource file name part.
     *
     * @param unitType unit type
     * @return lowercase unit type name used in resource paths
     */
    private String toUnitTypeName(UnitType unitType) {
        return switch (unitType) {
            case INFANTRY -> "infantry";
            case ARCHERS -> "archers";
            case CAVALRY -> "cavalry";
            case ARTILLERY -> "artillery";
        };
    }
}