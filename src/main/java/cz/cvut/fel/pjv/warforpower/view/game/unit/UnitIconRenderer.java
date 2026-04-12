package cz.cvut.fel.pjv.warforpower.view.game.unit;

import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.view.ScreenPosition;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Renders unit sprite images on a Canvas.
 */
public final class UnitIconRenderer {
    private static final double ICON_WIDTH = 35;
    private static final double ICON_HEIGHT = 35;

    private static final UnitImageProvider IMAGE_PROVIDER = new UnitImageProvider();

    private UnitIconRenderer() {
    }

    /**
     * Draws a unit icon centered at the specified screen position.
     *
     * @param gc graphics context to draw on
     * @param unit unit to render
     * @param position center position of the icon
     */
    public static void draw(GraphicsContext gc, Unit unit, ScreenPosition position) {
        if (gc == null) {
            throw new IllegalArgumentException("Graphics context cannot be null.");
        }
        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null.");
        }

        Image image = IMAGE_PROVIDER.getUnitImage(unit.getUnitType(), unit.getOwner().getColor());

        double drawX = position.x() - ICON_WIDTH / 2;
        double drawY = position.y() - ICON_HEIGHT / 2;

        gc.drawImage(image, drawX, drawY, ICON_WIDTH, ICON_HEIGHT);
    }
}