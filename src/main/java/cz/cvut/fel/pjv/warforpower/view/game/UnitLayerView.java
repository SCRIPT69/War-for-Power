package cz.cvut.fel.pjv.warforpower.view.game;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * Responsible for rendering units on top of the game map.
 * This layer is separate from tile rendering to keep map and unit visuals decoupled.
 */
public class UnitLayerView {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final HexTilesPositionGenerator positionGenerator;
    private final UnitImageProvider unitImageProvider;

    public UnitLayerView() {
        this.canvas = new Canvas(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.positionGenerator = new HexTilesPositionGenerator();
        this.unitImageProvider = new UnitImageProvider();
    }

    /**
     * Returns the canvas used for unit rendering.
     *
     * @return unit layer canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Renders all units currently present on the map.
     *
     * @param gameMap current game map
     */
    public void renderUnits(GameMap gameMap) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int rowIndex = 0; rowIndex < gameMap.getRowsNumber(); rowIndex++) {
            for (int tileIndex = 0; tileIndex < gameMap.getRowLength(rowIndex); tileIndex++) {
                HexTileCoords coords = new HexTileCoords(rowIndex, tileIndex);
                HexTile tile = gameMap.getTile(coords);

                if (tile instanceof OccupiableTile occupiableTile && occupiableTile.hasUnits()) {
                    ScreenPosition tilePosition = positionGenerator.getTilePosition(rowIndex, tileIndex);
                    renderUnitsOnTile(occupiableTile, tilePosition);
                }
            }
        }
    }

    /**
     * Renders units standing on a single occupiable tile.
     * The exact placement strategy may be extended later.
     *
     * @param tile occupiable tile
     * @param tilePosition screen position of the tile
     */
    private void renderUnitsOnTile(OccupiableTile tile, ScreenPosition tilePosition) {
        // TODO: draw one unit near the center-bottom, or two units top/bottom.
        for (Unit unit : tile.getStandingUnits()) {
            // TODO: render unit sprite using UnitImageProvider.
        }
    }
}