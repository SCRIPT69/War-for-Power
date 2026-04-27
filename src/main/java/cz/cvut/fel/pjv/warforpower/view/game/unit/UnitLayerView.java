package cz.cvut.fel.pjv.warforpower.view.game.unit;

import cz.cvut.fel.pjv.warforpower.model.map.GameMap;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTile;
import cz.cvut.fel.pjv.warforpower.model.tiles.HexTileCoords;
import cz.cvut.fel.pjv.warforpower.model.tiles.OccupiableTile;
import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import cz.cvut.fel.pjv.warforpower.view.game.tiles.HexTilesPositionGenerator;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Set;
import java.util.List;

/**
 * Responsible for rendering units on top of the game map.
 * This layer is separate from tile rendering to keep map and unit visuals decoupled.
 */
public class UnitLayerView {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private final HexTilesPositionGenerator positionGenerator;
    private static final double UNIT_HIT_RADIUS = 20;
    private static final double SELECTION_FRAME_WIDTH = 36;
    private static final double SELECTION_FRAME_HEIGHT = 36;

    private Set<Unit> selectedUnits = Set.of();

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


    private GameMap lastRenderedMap;

    private final List<UnitMoveAnimation> activeAnimations = new ArrayList<>();
    private final AnimationTimer movementTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            if (activeAnimations.isEmpty()) {
                stop();
                return;
            }

            activeAnimations.removeIf(animation -> animation.isFinished(now));

            if (lastRenderedMap != null) {
                renderUnits(lastRenderedMap);
            }

            if (activeAnimations.isEmpty()) {
                stop();
            }
        }
    };

    /**
     * Starts smooth movement animation of the specified unit
     * between two tile positions.
     *
     * @param unit animated unit
     * @param fromTilePosition source tile screen position
     * @param toTilePosition target tile screen position
     */
    public void animateUnitMovement(Unit unit,
                                    ScreenPosition fromTilePosition,
                                    ScreenPosition toTilePosition) {
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }
        if (fromTilePosition == null || toTilePosition == null) {
            throw new IllegalArgumentException("Animation positions cannot be null.");
        }

        ScreenPosition from = UnitPositionCalculator.forTile(fromTilePosition, 1).getFirst();
        ScreenPosition to = UnitPositionCalculator.forTile(toTilePosition, 1).getFirst();

        activeAnimations.add(new UnitMoveAnimation(
                unit,
                from,
                to,
                System.nanoTime(),
                220_000_000L
        ));

        movementTimer.start();
    }

    private boolean isUnitAnimating(Unit unit) {
        for (UnitMoveAnimation animation : activeAnimations) {
            if (animation.unit == unit) {
                return true;
            }
        }
        return false;
    }

    private void renderActiveAnimations(long now) {
        for (UnitMoveAnimation animation : activeAnimations) {
            ScreenPosition currentPosition = animation.getCurrentPosition(now);
            UnitIconRenderer.draw(gc, animation.unit, currentPosition);

            if (selectedUnits.contains(animation.unit)) {
                drawSelectionFrame(currentPosition);
            }
        }
    }


    /**
     * Updates currently selected units for highlight rendering.
     *
     * @param selectedUnits selected units
     */
    public void setSelectedUnits(List<Unit> selectedUnits) {
        this.selectedUnits = Set.copyOf(selectedUnits);
    }

    /**
     * Renders all units currently present on the map.
     *
     * @param gameMap current game map
     */
    public void renderUnits(GameMap gameMap) {
        this.lastRenderedMap = gameMap;

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

        renderActiveAnimations(System.nanoTime());
    }

    /**
     * Renders units standing on a single tile.
     * One unit is drawn slightly below center; two units are drawn above and below center.
     * Selected units are additionally highlighted with a gold selection frame.
     *
     * @param tile occupiable tile with units
     * @param tilePosition top-left screen position of the tile
     */
    private void renderUnitsOnTile(OccupiableTile tile, ScreenPosition tilePosition) {
        List<Unit> units = tile.getStandingUnits();
        List<ScreenPosition> positions =
                UnitPositionCalculator.forTile(tilePosition, units.size());

        for (int i = 0; i < units.size(); i++) {
            Unit unit = units.get(i);

            if (isUnitAnimating(unit)) {
                continue;
            }

            ScreenPosition position = positions.get(i);
            UnitIconRenderer.draw(gc, unit, position);

            if (selectedUnits.contains(unit)) {
                drawSelectionFrame(position);
            }
        }
    }
    /**
     * Draws a gold rectangular selection frame around a selected unit.
     *
     * @param center center position of the unit icon
     */
    private void drawSelectionFrame(ScreenPosition center) {
        double frameX = center.x() - SELECTION_FRAME_WIDTH / 2;
        double frameY = center.y() - SELECTION_FRAME_HEIGHT / 2;

        gc.setStroke(Color.GOLD);
        gc.setLineWidth(2.5);
        gc.strokeRect(frameX, frameY, SELECTION_FRAME_WIDTH, SELECTION_FRAME_HEIGHT);
    }

    /**
     * Returns the unit whose icon contains the specified screen coordinates.
     *
     * @param gameMap current game map
     * @param mouseX mouse x coordinate
     * @param mouseY mouse y coordinate
     * @return clicked unit or null if none was hit
     */
    public Unit findUnitAt(GameMap gameMap, double mouseX, double mouseY) {
        for (int rowIndex = 0; rowIndex < gameMap.getRowsNumber(); rowIndex++) {
            for (int tileIndex = 0; tileIndex < gameMap.getRowLength(rowIndex); tileIndex++) {
                HexTileCoords coords = new HexTileCoords(rowIndex, tileIndex);
                HexTile tile = gameMap.getTile(coords);

                if (!(tile instanceof OccupiableTile occupiableTile) || !occupiableTile.hasUnits()) {
                    continue;
                }

                ScreenPosition tilePosition = positionGenerator.getTilePosition(rowIndex, tileIndex);
                List<Unit> units = occupiableTile.getStandingUnits();
                List<ScreenPosition> positions = UnitPositionCalculator.forTile(tilePosition, units.size());

                for (int i = 0; i < units.size(); i++) {
                    if (isInsideUnitIcon(mouseX, mouseY, positions.get(i))) {
                        return units.get(i);
                    }
                }
            }
        }
        return null;
    }
    /**
     * Returns whether the specified screen point lies inside the unit hit area.
     *
     * @param mouseX mouse x coordinate
     * @param mouseY mouse y coordinate
     * @param center unit icon center
     * @return true if the point hits the unit icon
     */
    private boolean isInsideUnitIcon(double mouseX, double mouseY, ScreenPosition center) {
        double dx = mouseX - center.x();
        double dy = mouseY - center.y();
        return dx * dx + dy * dy <= UNIT_HIT_RADIUS * UNIT_HIT_RADIUS;
    }

    private static class UnitMoveAnimation {
        private final Unit unit;
        private final ScreenPosition from;
        private final ScreenPosition to;
        private final long startNanos;
        private final long durationNanos;

        private UnitMoveAnimation(Unit unit,
                                  ScreenPosition from,
                                  ScreenPosition to,
                                  long startNanos,
                                  long durationNanos) {
            this.unit = unit;
            this.from = from;
            this.to = to;
            this.startNanos = startNanos;
            this.durationNanos = durationNanos;
        }

        private boolean isFinished(long now) {
            return now - startNanos >= durationNanos;
        }

        private ScreenPosition getCurrentPosition(long now) {
            double progress = Math.min(1.0, (double) (now - startNanos) / durationNanos);

            double eased = 1 - Math.pow(1 - progress, 2); // ease-out

            double x = from.x() + (to.x() - from.x()) * eased;
            double y = from.y() + (to.y() - from.y()) * eased;

            return new ScreenPosition(x, y);
        }
    }
}
