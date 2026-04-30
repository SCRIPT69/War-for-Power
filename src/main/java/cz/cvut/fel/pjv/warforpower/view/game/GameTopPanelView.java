package cz.cvut.fel.pjv.warforpower.view.game;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Renders the top HUD panel containing current player info,
 * round number, turn timer and end-turn controls.
 */
public class GameTopPanelView {
    private final HBox root = new HBox();

    private final HBox leftBox = new HBox(20);
    private final HBox rightBox = new HBox(14);

    private final Label currentPlayerLabel = new Label("Player 1");
    private final Label coinsLabel = new Label("Coins: 50");
    private final Label roundLabel = new Label("Round 1");
    private final Button endTurnButton = new Button("End Turn");
    private final TurnTimerView turnTimerView = new TurnTimerView();

    public GameTopPanelView(double width) {
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPrefWidth(width);
        root.setPadding(new Insets(0));

        leftBox.setAlignment(Pos.CENTER_LEFT);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        currentPlayerLabel.setStyle(
                "-fx-font-size: 34px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #ff2a00;"
        );

        coinsLabel.setStyle(commonHudLabelStyle());
        roundLabel.setStyle(commonHudLabelStyle());

        currentPlayerLabel.setMinWidth(150);
        coinsLabel.setMinWidth(140);
        roundLabel.setMinWidth(150);

        turnTimerView.getRoot().setMinWidth(55);
        turnTimerView.getRoot().setPrefWidth(55);
        turnTimerView.getRoot().setMaxWidth(55);

        endTurnButton.setMinWidth(150);
        endTurnButton.setPrefWidth(150);

        applyEndTurnButtonDefaultStyle();

        endTurnButton.setOnMouseEntered(event -> applyEndTurnButtonHoverStyle());
        endTurnButton.setOnMouseExited(event -> applyEndTurnButtonDefaultStyle());
        endTurnButton.setOnMousePressed(event -> applyEndTurnButtonPressedStyle());
        endTurnButton.setOnMouseReleased(event -> {
            if (endTurnButton.isHover()) {
                applyEndTurnButtonHoverStyle();
            } else {
                applyEndTurnButtonDefaultStyle();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        leftBox.getChildren().addAll(
                currentPlayerLabel,
                coinsLabel,
                roundLabel
        );

        rightBox.getChildren().addAll(
                turnTimerView.getRoot(),
                endTurnButton
        );

        root.getChildren().addAll(
                leftBox,
                spacer,
                rightBox
        );
    }

    public HBox getRoot() {
        return root;
    }

    public Button getEndTurnButton() {
        return endTurnButton;
    }

    /**
     * Enables or disables the end turn button.
     *
     * @param disabled true if the button should be disabled
     */
    public void setEndTurnButtonDisabled(boolean disabled) {
        endTurnButton.setDisable(disabled);
    }

    public void update(String playerName, String playerColorCss, int coins, int round) {
        currentPlayerLabel.setText(playerName);
        currentPlayerLabel.setStyle(
                "-fx-font-size: 34px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + playerColorCss + ";"
        );

        coinsLabel.setText("Coins: " + coins);
        roundLabel.setText("Round " + round);
    }

    private void applyEndTurnButtonDefaultStyle() {
        endTurnButton.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #3f2c1b;" +
                        "-fx-background-color: linear-gradient(to bottom, #e6d2a2, #c9ab6d);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: #7a5a32;" +
                        "-fx-border-width: 2;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.28), 8, 0.2, 0, 2);" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10 22 10 22;"
        );
    }

    private void applyEndTurnButtonHoverStyle() {
        endTurnButton.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #3f2c1b;" +
                        "-fx-background-color: linear-gradient(to bottom, #f0ddb0, #d6b978);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: #7a5a32;" +
                        "-fx-border-width: 2;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 10, 0.22, 0, 3);" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 10 22 10 22;"
        );
    }

    private void applyEndTurnButtonPressedStyle() {
        endTurnButton.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #3f2c1b;" +
                        "-fx-background-color: linear-gradient(to bottom, #c9ab6d, #b59257);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-color: #6a4d2a;" +
                        "-fx-border-width: 2;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 4, 0.15, 0, 1);" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 11 22 9 22;"
        );
    }

    private String commonHudLabelStyle() {
        return "-fx-font-size: 32px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #3f2c1b;";
    }

    /**
     * Updates displayed remaining turn time.
     *
     * @param seconds remaining seconds
     */
    public void updateTimer(int seconds) {
        turnTimerView.updateTime(seconds);
    }

    /**
     * Resets displayed timer to default value.
     */
    public void resetTimer() {
        turnTimerView.reset();
    }
}