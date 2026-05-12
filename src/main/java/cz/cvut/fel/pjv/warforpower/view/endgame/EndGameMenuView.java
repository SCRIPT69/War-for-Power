package cz.cvut.fel.pjv.warforpower.view.endgame;

import cz.cvut.fel.pjv.warforpower.model.players.Player;
import cz.cvut.fel.pjv.warforpower.model.score.GameScoreResult;
import cz.cvut.fel.pjv.warforpower.model.score.ScoreResult;
import cz.cvut.fel.pjv.warforpower.view.PlayerColorCssMapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Comparator;
import java.util.List;

/**
 * Overlay menu shown after the game ends.
 * Displays winner or winners and final score of every player.
 */
public class EndGameMenuView {
    private final StackPane root = new StackPane();
    private final VBox card = new VBox(18);

    private final Label titleLabel = new Label("Game Over");
    private final HBox winnersBox = new HBox(6);
    private final VBox scoresBox = new VBox(10);
    private final Button exitButton = new Button("Main Menu");

    public EndGameMenuView() {
        root.setVisible(false);
        root.setManaged(false);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.25);");

        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(42));
        card.setPrefWidth(680);
        card.setMaxWidth(680);
        card.setPrefHeight(420);
        card.setMaxHeight(420);

        card.setStyle("""
        -fx-background-color: rgba(20, 20, 20, 0.92);
        -fx-background-radius: 18;
        -fx-border-color: rgba(255, 255, 255, 0.35);
        -fx-border-width: 2;
        -fx-border-radius: 18;
        """);

        titleLabel.setStyle("""
                -fx-font-size: 38px;
                -fx-font-weight: bold;
                -fx-text-fill: white;
                """);

        winnersBox.setAlignment(Pos.CENTER);
        scoresBox.setAlignment(Pos.CENTER);

        exitButton.setMinWidth(110);
        exitButton.setStyle("""
                -fx-font-size: 16px;
                -fx-font-weight: bold;
                -fx-padding: 9 26 9 26;
                """);

        card.getChildren().addAll(
                titleLabel,
                winnersBox,
                scoresBox,
                exitButton
        );

        root.getChildren().add(card);
    }

    public StackPane getRoot() {
        return root;
    }

    /**
     * Shows the end-game menu with final score data.
     *
     * @param result final calculated game score result
     * @param onBackToMenu action invoked when the main menu button is pressed
     */
    public void show(GameScoreResult result, Runnable onBackToMenu) {
        if (result == null) {
            throw new IllegalArgumentException("Game score result cannot be null.");
        }

        winnersBox.getChildren().clear();
        scoresBox.getChildren().clear();

        winnersBox.getChildren().add(buildWinnersNode(result.winners()));

        result.playerScores().stream()
                .sorted(Comparator.comparingInt(ScoreResult::getTotalPoints).reversed())
                .forEach(scoreResult -> scoresBox.getChildren().add(buildScoreRow(scoreResult)));

        exitButton.setOnAction(event -> {
            if (onBackToMenu != null) {
                onBackToMenu.run();
            }
        });

        root.setVisible(true);
        root.setManaged(true);
    }

    /**
     * Hides the end-game menu.
     */
    public void hide() {
        root.setVisible(false);
        root.setManaged(false);
    }

    /**
     * Builds winner display row.
     * Player names are rendered using their player colors instead of showing
     * the color name as text.
     *
     * @param winners final winner list
     * @return winner row node
     */
    private HBox buildWinnersNode(List<Player> winners) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER);

        Label prefixLabel = new Label(winners != null && winners.size() > 1 ? "Winners:" : "Winner:");
        prefixLabel.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: gold;
                """);

        box.getChildren().add(prefixLabel);

        if (winners == null || winners.isEmpty()) {
            Label noWinnerLabel = new Label("No winner");
            noWinnerLabel.setStyle("""
                    -fx-font-size: 24px;
                    -fx-font-weight: bold;
                    -fx-text-fill: white;
                    """);

            box.getChildren().add(noWinnerLabel);
            return box;
        }

        for (int i = 0; i < winners.size(); i++) {
            Player winner = winners.get(i);

            Label playerLabel = new Label(winner.getName());
            playerLabel.setStyle("""
                    -fx-font-size: 24px;
                    -fx-font-weight: bold;
                    -fx-text-fill: %s;
                    """.formatted(PlayerColorCssMapper.toCssColor(winner.getColor())));

            box.getChildren().add(playerLabel);

            if (i < winners.size() - 1) {
                Label commaLabel = new Label(",");
                commaLabel.setStyle("""
                        -fx-font-size: 24px;
                        -fx-font-weight: bold;
                        -fx-text-fill: gold;
                        """);

                box.getChildren().add(commaLabel);
            }
        }

        return box;
    }

    /**
     * Builds one score row for a player.
     * The player name is colored according to the player's color.
     *
     * @param scoreResult score result of one player
     * @return score row node
     */
    private HBox buildScoreRow(ScoreResult scoreResult) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER);

        Label playerLabel = new Label(scoreResult.player().getName());
        playerLabel.setStyle("""
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-text-fill: %s;
                """.formatted(PlayerColorCssMapper.toCssColor(scoreResult.player().getColor())));

        Label scoreLabel = new Label(
                "— "
                        + scoreResult.getTotalPoints()
                        + " points"
                        + " | bases: "
                        + scoreResult.basePoints()
                        + ", territory: "
                        + scoreResult.connectedTilePoints()
        );

        scoreLabel.setStyle("""
                -fx-font-size: 18px;
                -fx-text-fill: white;
                """);

        row.getChildren().addAll(playerLabel, scoreLabel);

        return row;
    }
}