package cz.cvut.fel.pjv.warforpower.view.endgame;

import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.score.GameScoreResult;
import cz.cvut.fel.pjv.warforpower.model.score.ScoreResult;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.stream.Collectors;

/**
 * View responsible for displaying final game result,
 * winners and score summary of all players.
 */
public class EndGameView {
    private final VBox root = new VBox(16);

    private final Label titleLabel = new Label("Game Over");
    private final Label winnersLabel = new Label("-");
    private final Label scoreSummaryLabel = new Label("-");
    private final Button returnToMenuButton = new Button("Return to Menu");

    public EndGameView() {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        titleLabel.setStyle(
                "-fx-font-size: 32px;" +
                        "-fx-font-weight: bold;"
        );

        scoreSummaryLabel.setWrapText(true);
        scoreSummaryLabel.setMaxWidth(550);

        root.getChildren().addAll(
                titleLabel,
                winnersLabel,
                scoreSummaryLabel,
                returnToMenuButton
        );
    }

    /**
     * Returns the root node of the end-game view.
     *
     * @return root node
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * Displays final game score result and winners.
     *
     * @param gameScoreResult final score result
     */
    public void showResult(GameScoreResult gameScoreResult) {
        if (gameScoreResult == null) {
            throw new IllegalArgumentException("Game score result cannot be null.");
        }

        String winnersText = gameScoreResult.winners().stream()
                .map(Player::getName)
                .collect(Collectors.joining(", "));
        winnersLabel.setText("Winner(s): " + winnersText);

        StringBuilder summaryBuilder = new StringBuilder();
        for (ScoreResult scoreResult : gameScoreResult.playerScores()) {
            summaryBuilder.append(scoreResult.player().getName())
                    .append(" - total: ")
                    .append(scoreResult.getTotalPoints())
                    .append(" (bases: ")
                    .append(scoreResult.basePoints())
                    .append(", connected tiles: ")
                    .append(scoreResult.connectedTilePoints())
                    .append(")")
                    .append(System.lineSeparator());
        }

        scoreSummaryLabel.setText(summaryBuilder.toString());
    }

    /**
     * Returns button used to return to the main menu.
     *
     * @return return-to-menu button
     */
    public Button getReturnToMenuButton() {
        return returnToMenuButton;
    }
}