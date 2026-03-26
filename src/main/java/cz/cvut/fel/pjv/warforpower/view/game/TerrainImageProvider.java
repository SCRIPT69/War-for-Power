package cz.cvut.fel.pjv.warforpower.view.game;

import cz.cvut.fel.pjv.warforpower.model.players.PlayerColor;
import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainType;
import javafx.scene.image.Image;

public class TerrainImageProvider {
    private final Image plainsImage = new Image(getClass().getResource("/img/tiles/terrains/plains.png").toExternalForm());
    private final Image desertImage = new Image(getClass().getResource("/img/tiles/terrains/desert.png").toExternalForm());
    private final Image mountainImage = new Image(getClass().getResource("/img/tiles/terrains/mountain.png").toExternalForm());
    private final Image forestImage = new Image(getClass().getResource("/img/tiles/terrains/forest.png").toExternalForm());

    private final Image redBaseImage = new Image(getClass().getResource("/img/tiles/bases/red_base.png").toExternalForm());
    private final Image blueBaseImage = new Image(getClass().getResource("/img/tiles/bases/blue_base.png").toExternalForm());
    private final Image lightblueBaseImage = new Image(getClass().getResource("/img/tiles/bases/lightblue_base.png").toExternalForm());
    private final Image purpleBaseImage = new Image(getClass().getResource("/img/tiles/bases/purple_base.png").toExternalForm());

    private final Image cityImage = new Image(getClass().getResource("/img/tiles/city.png").toExternalForm());

    public Image getTerrainImage(TerrainType terrainType) {
        return switch (terrainType) {
            case PLAINS -> plainsImage;
            case DESERT -> desertImage;
            case MOUNTAIN -> mountainImage;
            case FOREST -> forestImage;
        };
    }

    public Image getBaseImage(PlayerColor playerColor) {
        return switch (playerColor) {
            case RED -> redBaseImage;
            case BLUE -> blueBaseImage;
            case LIGHTBLUE -> lightblueBaseImage;
            case PURPLE -> purpleBaseImage;
        };
    }


    public Image getCityImage() {
        return cityImage;
    }
}
