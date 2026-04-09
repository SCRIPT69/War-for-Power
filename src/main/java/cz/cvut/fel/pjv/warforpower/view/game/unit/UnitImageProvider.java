package cz.cvut.fel.pjv.warforpower.view.game.unit;

import cz.cvut.fel.pjv.warforpower.model.players.PlayerColor;
import cz.cvut.fel.pjv.warforpower.model.units.UnitType;
import javafx.scene.image.Image;

import java.net.URL;

/**
 * Provides images used for rendering units on the map.
 */
public class UnitImageProvider {

    /**
     * Returns image corresponding to the specified unit type and owner color.
     *
     * @param unitType unit type
     * @param playerColor owner color
     * @return unit image
     */
    public Image getUnitImage(UnitType unitType, PlayerColor playerColor) {
        String imageUrl = "/img/units/";
        String color = switch (playerColor) {
            case RED -> "red";
            case BLUE -> "blue";
            case LIGHTBLUE -> "lightblue";
            case PURPLE -> "purple";
        };

        imageUrl += color + "/" + color + "_";

        String unitTypeName = switch (unitType) {
            case INFANTRY -> "infantry";
            case ARCHERS -> "archers";
            case CAVALRY -> "cavalry";
            case ARTILLERY -> "artillery";
        };
        imageUrl += unitTypeName + ".png";

        URL resource = getClass().getResource(imageUrl);
        if (resource == null) {
            throw new IllegalArgumentException("Unit image resource not found: " + imageUrl);
        }

        return new Image(resource.toExternalForm());
    }
}