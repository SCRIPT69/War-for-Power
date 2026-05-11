package cz.cvut.fel.pjv.warforpower.view.game.battle.dice;

import javafx.scene.image.Image;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides dice face images used in battle overlay.
 */
public class DiceImageProvider {
    private final Map<Integer, Image> diceFaces = new HashMap<>();

    public DiceImageProvider() {
        for (int face = 1; face <= 6; face++) {
            diceFaces.put(face, load("/img/dice/dice_" + face + ".png"));
        }
    }

    /**
     * Returns image for the specified dice face.
     *
     * @param face dice face value from 1 to 6
     * @return dice face image
     */
    public Image getDiceFace(int face) {
        if (face < 1 || face > 6) {
            throw new IllegalArgumentException("Dice face must be between 1 and 6.");
        }
        return diceFaces.get(face);
    }

    private Image load(String path) {
        return new Image(getClass().getResource(path).toExternalForm());
    }
}