package cz.cvut.fel.pjv.warforpower.view.game;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/**
 * Small HUD component displaying remaining turn time.
 */
public class TurnTimerView {
    private final StackPane root = new StackPane();
    private final Label timerLabel = new Label("60");

    public TurnTimerView() {
        root.setMinWidth(60);
        root.setPrefWidth(60);
        root.setAlignment(Pos.CENTER);

        timerLabel.setStyle(
                "-fx-font-size: 28px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #3f2c1b;"
        );

        root.getChildren().add(timerLabel);
    }

    /**
     * Returns the root node of the timer view.
     *
     * @return root node
     */
    public StackPane getRoot() {
        return root;
    }

    /**
     * Updates displayed remaining turn time.
     *
     * @param seconds remaining seconds
     */
    public void updateTime(int seconds) {
        timerLabel.setText(String.valueOf(seconds));
    }

    /**
     * Clears timer display to default value.
     */
    public void reset() {
        timerLabel.setText("60");
    }
}