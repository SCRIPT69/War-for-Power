package cz.cvut.fel.pjv.warforpower.view.game;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.players.PlayerColor;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.Ownable;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

/**
 * Responsible for rendering ownership markers on capturable tiles.
 * Ownership is displayed as a small flag overlay at the center of the tile.
 */
public class TileOwnershipLayerView {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final HexTilesPositionGenerator positionGenerator;
    private final OwnershipFlagImageProvider flagImageProvider;

    public TileOwnershipLayerView() {
        this.canvas = new Canvas(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        this.gc = canvas.getGraphicsContext2D();
        this.positionGenerator = new HexTilesPositionGenerator();
        this.flagImageProvider = new OwnershipFlagImageProvider();
    }

    /**
     * Returns the canvas used for ownership marker rendering.
     *
     * @return ownership layer canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Renders ownership markers for all ownable tiles currently present on the map.
     *
     * @param gameMap current game map
     */
    public void renderOwnership(GameMap gameMap) {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int rowIndex = 0; rowIndex < gameMap.getRowsNumber(); rowIndex++) {
            for (int tileIndex = 0; tileIndex < gameMap.getRowLength(rowIndex); tileIndex++) {
                HexTileCoords coords = new HexTileCoords(rowIndex, tileIndex);
                HexTile tile = gameMap.getTile(coords);

                if (tile instanceof Ownable ownableTile && ownableTile.getOwner() != null) {
                    ScreenPosition tilePosition = positionGenerator.getTilePosition(rowIndex, tileIndex);
                    renderFlag(tilePosition, ownableTile.getOwner().getColor());
                }
            }
        }
    }

    /**
     * Renders ownership flag at the center of a tile.
     *
     * @param tilePosition tile screen position
     * @param playerColor owner color
     */
    private void renderFlag(ScreenPosition tilePosition, PlayerColor playerColor) {
        // TODO: draw ownership flag image in the tile center.
    }
}