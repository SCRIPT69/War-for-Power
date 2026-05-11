package cz.cvut.fel.pjv.warforpower.view.game.battle.dice;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Renders and animates a horizontal row of dice.
 */
public class DiceRowView {
    private static final double DICE_SIZE = 42;

    private final HBox root = new HBox(8);
    private final DiceImageProvider diceImageProvider = new DiceImageProvider();
    private final Random random = new Random();

    public DiceRowView() {
        root.setAlignment(Pos.CENTER);
    }

    public HBox getRoot() {
        return root;
    }

    /**
     * Clears all currently displayed dice.
     */
    public void clear() {
        root.getChildren().clear();
    }

    /**
     * Shows final rolled dice faces without animation.
     *
     * @param values dice values
     */
    public void showDice(List<Integer> values) {
        clear();

        for (Integer value : values) {
            ImageView diceView = createDiceView();
            diceView.setImage(diceImageProvider.getDiceFace(value));
            root.getChildren().add(diceView);
        }
    }

    /**
     * Plays simple rolling animation and then shows final dice values.
     *
     * @param finalValues final dice values
     * @param onFinished action invoked after animation finishes
     */
    public void playRollAnimation(List<Integer> finalValues, Runnable onFinished) {
        clear();

        if (finalValues == null || finalValues.isEmpty()) {
            if (onFinished != null) {
                onFinished.run();
            }
            return;
        }

        List<ImageView> diceViews = new ArrayList<>();
        for (int i = 0; i < finalValues.size(); i++) {
            ImageView diceView = createDiceView();
            diceView.setImage(diceImageProvider.getDiceFace(randomFace()));
            diceViews.add(diceView);
            root.getChildren().add(diceView);
        }

        Timeline timeline = new Timeline();
        for (int step = 0; step < 8; step++) {
            timeline.getKeyFrames().add(new KeyFrame(Duration.millis(step * 70L), event -> {
                for (ImageView diceView : diceViews) {
                    diceView.setImage(diceImageProvider.getDiceFace(randomFace()));
                }
            }));
        }

        timeline.setOnFinished(event -> {
            for (int i = 0; i < finalValues.size(); i++) {
                diceViews.get(i).setImage(diceImageProvider.getDiceFace(finalValues.get(i)));
            }
            if (onFinished != null) {
                onFinished.run();
            }
        });

        timeline.play();
    }

    private ImageView createDiceView() {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(DICE_SIZE);
        imageView.setFitHeight(DICE_SIZE);
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private int randomFace() {
        return random.nextInt(6) + 1;
    }
}