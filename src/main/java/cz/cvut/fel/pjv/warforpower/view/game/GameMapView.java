package cz.cvut.fel.pjv.warforpower.view.game;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainTile;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class GameMapView {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final HexTilesPositionGenerator positionGenerator;
    private final TerrainImageProvider terrainImageProvider;
    private final Image backgroundImage =
            new Image(getClass().getResource("/img/game_field.png").toExternalForm());

    public GameMapView() {
        canvas = new Canvas(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.positionGenerator = new HexTilesPositionGenerator();
        this.terrainImageProvider = new TerrainImageProvider();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void renderMap(GameMap gameMap) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.drawImage(backgroundImage, 0, 0, UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);

        for (int rowIndex = 0; rowIndex < gameMap.getRowsNumber(); rowIndex++) {
            for (int tileIndex = 0; tileIndex < gameMap.getRowLength(rowIndex); tileIndex++) {
                HexTileCoords coords = new HexTileCoords(rowIndex, tileIndex);
                HexTile tile = gameMap.getTile(coords);

                ScreenPosition position = positionGenerator.getTilePosition(rowIndex, tileIndex);

                drawTile(tile, position.x(), position.y());
            }
        }
    }

    private void drawTile(HexTile tile, double x, double y) {
        Image image = resolveTileImage(tile);
        gc.drawImage(image, x, y, 71, 80);
    }

    private Image resolveTileImage(HexTile tile) {
        return switch (tile.getTileType()) {
            case TERRAIN -> {
                TerrainTile terrainTile = (TerrainTile) tile;
                yield terrainImageProvider.getTerrainImage(terrainTile.getTerrainType());
            }
            case BASE -> {
                BaseTile baseTile = (BaseTile) tile;
                yield terrainImageProvider.getBaseImage(baseTile.getOwner().getColor());
            }
            case CITY -> terrainImageProvider.getCityImage();
        };
    }
}
