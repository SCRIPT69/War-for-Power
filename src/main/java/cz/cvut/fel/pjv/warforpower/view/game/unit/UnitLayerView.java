package cz.cvut.fel.pjv.warforpower.view.game.unit;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import cz.cvut.fel.pjv.warforpower.view.game.tiles.HexTilesPositionGenerator;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * Responsible for rendering units on top of the game map.
 * This layer is separate from tile rendering to keep map and unit visuals decoupled.
 */
public class UnitLayerView {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final HexTilesPositionGenerator positionGenerator;

    public UnitLayerView() {
        this.canvas = new Canvas(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        this.canvas.setMouseTransparent(true);
        this.gc = canvas.getGraphicsContext2D();
        this.positionGenerator = new HexTilesPositionGenerator();
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
                    ScreenPosition tilePos = positionGenerator.getTilePosition(rowIndex, tileIndex);
                    renderUnitsOnTile(occupiableTile, tilePos);
                }
            }
        }
    }

    /**
     * Renders units standing on a single tile.
     * One unit is drawn slightly below center; two units are drawn above and below center.
     *
     * @param tile        occupiable tile with units
     * @param tilePosition top-left screen position of the tile
     */
    private void renderUnitsOnTile(OccupiableTile tile, ScreenPosition tilePosition) {
        List<Unit> units = tile.getStandingUnits();
        List<ScreenPosition> positions =
                UnitPositionCalculator.forTile(tilePosition, units.size());

        for (int i = 0; i < units.size(); i++) {
            UnitIconRenderer.draw(gc, units.get(i), positions.get(i));
        }
    }
}