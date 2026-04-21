package cz.cvut.fel.pjv.warforpower.view.game;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.tiles.BaseTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.TerrainTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import cz.cvut.fel.pjv.warforpower.view.game.tiles.HexTilesPositionGenerator;
import cz.cvut.fel.pjv.warforpower.view.game.tiles.TerrainImageProvider;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Responsible for rendering the game map, tiles, highlights
 * and mouse interaction on the game canvas.
 */
public class GameMapView {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final HexTilesPositionGenerator positionGenerator;
    private final TerrainImageProvider terrainImageProvider;
    private final Image backgroundImage =
            new Image(getClass().getResource("/img/game_field.png").toExternalForm());

    private final GameMap gameMap;

    private static final double TILE_WIDTH = 71;
    private static final double TILE_HEIGHT = 80;

    private final Set<HexTileCoords> highlightedTiles = new HashSet<>();
    private Predicate<HexTileCoords> isTileInteractive;
    private Consumer<HexTileCoords> onTileClicked;

    private Predicate<Unit> isUnitInteractive;
    private BiConsumer<Unit, Boolean> onUnitClicked;
    private BiFunction<Double, Double, Unit> unitAtResolver;

    public GameMapView(GameMap gameMap) {
        this.gameMap = gameMap;

        canvas = new Canvas(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        canvas.setOnMouseMoved(this::handleMouseMoved);
        canvas.setOnMouseClicked(this::handleMouseClicked);

        this.gc = canvas.getGraphicsContext2D();
        this.positionGenerator = new HexTilesPositionGenerator();
        this.terrainImageProvider = new TerrainImageProvider();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Sets highlighted tile coordinates to be rendered on the map.
     *
     * @param coords highlighted tile coordinates
     */
    public void setHighlightedTiles(Set<HexTileCoords> coords) {
        highlightedTiles.clear();
        highlightedTiles.addAll(coords);
    }

    public void setOnTileClicked(Consumer<HexTileCoords> onTileClicked) {

        this.onTileClicked = onTileClicked;
    }
    public void setTileInteractivePredicate(Predicate<HexTileCoords> isTileInteractive) {
        this.isTileInteractive = isTileInteractive;
    }

    public void addHighlightedTile(HexTileCoords coords) {
        highlightedTiles.add(coords);
    }

    public void clearHighlightedTiles() {
        highlightedTiles.clear();
    }


    /**
     * Registers predicate deciding whether a unit should be treated as interactive.
     *
     * @param isUnitInteractive predicate for unit interactivity
     */
    public void setUnitInteractivePredicate(Predicate<Unit> isUnitInteractive) {
        this.isUnitInteractive = isUnitInteractive;
    }

    /**
     * Registers unit click handler.
     *
     * @param onUnitClicked callback receiving clicked unit and shift key state
     */
    public void setOnUnitClicked(BiConsumer<Unit, Boolean> onUnitClicked) {
        this.onUnitClicked = onUnitClicked;
    }

    /**
     * Registers resolver used to find a unit at given screen coordinates.
     *
     * @param unitAtResolver resolver returning unit at mouse coordinates or null
     */
    public void setUnitAtResolver(BiFunction<Double, Double, Unit> unitAtResolver) {
        this.unitAtResolver = unitAtResolver;
    }

    /**
     * Renders the current map state on the canvas.
     */
    public void renderMap() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.drawImage(backgroundImage, 0, 0, UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);

        for (int rowIndex = 0; rowIndex < gameMap.getRowsNumber(); rowIndex++) {
            for (int tileIndex = 0; tileIndex < gameMap.getRowLength(rowIndex); tileIndex++) {
                HexTileCoords coords = new HexTileCoords(rowIndex, tileIndex);
                HexTile tile = gameMap.getTile(coords);

                ScreenPosition position = positionGenerator.getTilePosition(rowIndex, tileIndex);

                drawTile(tile, position.x(), position.y());
                if (highlightedTiles.contains(coords)) {
                    drawBaseHighlight(position.x(), position.y());
                }
            }
        }
    }

    /**
     * Handles mouse movement over the map canvas and updates cursor
     * according to interactive units or tiles under the pointer.
     *
     * @param event mouse event
     */
    private void handleMouseMoved(MouseEvent event) {
        if (unitAtResolver != null && isUnitInteractive != null) {
            Unit hoveredUnit = unitAtResolver.apply(event.getX(), event.getY());
            if (hoveredUnit != null && isUnitInteractive.test(hoveredUnit)) {
                canvas.setCursor(Cursor.HAND);
                return;
            }
        }

        HexTileCoords hoveredCoords = findTileAt(event.getX(), event.getY());
        if (hoveredCoords != null
                && isTileInteractive != null
                && isTileInteractive.test(hoveredCoords)) {
            canvas.setCursor(Cursor.HAND);
            return;
        }

        canvas.setCursor(Cursor.DEFAULT);
    }

    /**
     * Handles mouse click on the map canvas.
     * Unit click has priority over tile click.
     *
     * @param event mouse event
     */
    private void handleMouseClicked(MouseEvent event) {
        if (onUnitClicked != null && unitAtResolver != null) {
            Unit clickedUnit = unitAtResolver.apply(event.getX(), event.getY());
            if (clickedUnit != null) {
                onUnitClicked.accept(clickedUnit, event.isShiftDown());
                return;
            }
        }

        if (onTileClicked == null) {
            return;
        }

        HexTileCoords clickedCoords = findTileAt(event.getX(), event.getY());
        if (clickedCoords != null) {
            onTileClicked.accept(clickedCoords);
        }
    }

    /**
     * Returns screen position of the specified tile.
     *
     * @param coords tile coordinates
     * @return screen position of the tile
     */
    public ScreenPosition getTileScreenPosition(HexTileCoords coords) {
        return positionGenerator.getTilePosition(coords.rowIndex(), coords.tileIndex());
    }

    private boolean isPointInsidePolygon(double mouseX, double mouseY, double[] xPoints, double[] yPoints) {
        boolean inside = false;
        int pointsCount = xPoints.length;

        for (int i = 0, j = pointsCount - 1; i < pointsCount; j = i++) {
            boolean intersects = ((yPoints[i] > mouseY) != (yPoints[j] > mouseY))
                    && (mouseX < (xPoints[j] - xPoints[i]) * (mouseY - yPoints[i]) / (yPoints[j] - yPoints[i]) + xPoints[i]);

            if (intersects) {
                inside = !inside;
            }
        }

        return inside;
    }
    private HexTileCoords findTileAt(double mouseX, double mouseY) {
        for (int rowIndex = 0; rowIndex < gameMap.getRowsNumber(); rowIndex++) {
            for (int tileIndex = 0; tileIndex < gameMap.getRowLength(rowIndex); tileIndex++) {
                ScreenPosition position = positionGenerator.getTilePosition(rowIndex, tileIndex);

                double[] xPoints = getHexXPoints(position.x());
                double[] yPoints = getHexYPoints(position.y());

                if (isPointInsidePolygon(mouseX, mouseY, xPoints, yPoints)) {
                    return new HexTileCoords(rowIndex, tileIndex);
                }
            }
        }

        return null;
    }

    private void drawTile(HexTile tile, double x, double y) {
        Image image = resolveTileImage(tile);
        gc.drawImage(image, x, y, TILE_WIDTH, TILE_HEIGHT);
    }

    private double[] getHexXPoints(double x) {
        double sideOffset = -4.5;

        return new double[] {
                x + TILE_WIDTH / 2,
                x + TILE_WIDTH - sideOffset,
                x + TILE_WIDTH - sideOffset,
                x + TILE_WIDTH / 2,
                x + sideOffset,
                x + sideOffset
        };
    }
    private double[] getHexYPoints(double y) {
        double topOffset = -4.5;
        double cornerHeight = 18;

        return new double[] {
                y + topOffset,
                y + cornerHeight,
                y + TILE_HEIGHT - cornerHeight,
                y + TILE_HEIGHT - topOffset,
                y + TILE_HEIGHT - cornerHeight,
                y + cornerHeight
        };
    }
    private void drawBaseHighlight(double x, double y) {
        double topOffset = -4.5;
        double sideOffset = -4.5;
        double cornerHeight = 18;

        double[] xPoints = getHexXPoints(x);

        double[] yPoints = getHexYPoints(y);

        gc.setFill(Color.color(1.0, 0.84, 0.0, 0.22));
        gc.fillPolygon(xPoints, yPoints, 6);

        gc.setStroke(Color.GOLD);
        gc.setLineWidth(4);
        gc.strokePolygon(xPoints, yPoints, 6);
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
