package cz.cvut.fel.pjv.warforpower.view.game.battle;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Modal battle overlay shown above the game board.
 */
public class BattleOverlayView {
    private final StackPane root = new StackPane();

    private final Region dimBackground = new Region();
    private final VBox panel = new VBox(22);

    private final Label titleLabel = new Label("Battle");
    private final HBox sidesRow = new HBox(36);

    private final BattleSidePaneView attackerPane = new BattleSidePaneView();
    private final BattleSidePaneView defenderPane = new BattleSidePaneView();

    private final Label centerLabel = new Label("VS");
    private final Button continueButton = new Button("Continue");

    public BattleOverlayView() {
        root.setVisible(false);
        root.setManaged(false);
        root.setPickOnBounds(true);
        root.setAlignment(Pos.CENTER);

        dimBackground.setStyle("""
            -fx-background-color: rgba(0, 0, 0, 0.38);
            """);
        dimBackground.setMouseTransparent(false);
        dimBackground.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        panel.setAlignment(Pos.CENTER);
        panel.setPrefWidth(620);
        panel.setMaxWidth(620);
        panel.setMaxHeight(Region.USE_PREF_SIZE);
        panel.setPadding(new Insets(24));
        panel.setStyle("""
            -fx-background-color: #e3d4aa;
            -fx-border-color: #8b6f47;
            -fx-border-width: 2;
            -fx-border-radius: 18;
            -fx-background-radius: 18;
            """);

        titleLabel.setStyle("""
            -fx-font-size: 30px;
            -fx-font-weight: bold;
            -fx-text-fill: #3f2c1b;
            """);

        centerLabel.setStyle("""
            -fx-font-size: 28px;
            -fx-font-weight: bold;
            -fx-text-fill: #6a4d2a;
            """);

        continueButton.setStyle("""
            -fx-font-size: 20px;
            -fx-font-weight: bold;
            -fx-text-fill: #3f2c1b;
            -fx-background-color: linear-gradient(to bottom, #e6d2a2, #c9ab6d);
            -fx-background-radius: 14;
            -fx-border-radius: 14;
            -fx-border-color: #7a5a32;
            -fx-border-width: 2;
            -fx-cursor: hand;
            -fx-padding: 10 22 10 22;
            """);

        sidesRow.setAlignment(Pos.CENTER);
        sidesRow.getChildren().addAll(
                attackerPane.getRoot(),
                centerLabel,
                defenderPane.getRoot()
        );

        panel.getChildren().addAll(
                titleLabel,
                sidesRow,
                continueButton
        );

        StackPane.setAlignment(panel, Pos.CENTER);

        root.getChildren().addAll(dimBackground, panel);
    }

    public Parent getRoot() {
        return root;
    }

    /**
     * Updates overlay contents using provided data.
     *
     * @param data overlay data
     */
    public void setData(BattleOverlayData data) {
        if (data == null) {
            throw new IllegalArgumentException("Battle overlay data cannot be null.");
        }

        titleLabel.setText(data.title());
        attackerPane.update(data.attackerLabel(), data.attackers(), false);
        defenderPane.update(data.defenderLabel(), data.defenders(), data.defenderIsCity());
    }

    /**
     * Registers action for the bottom button.
     *
     * @param action continue action
     */
    public void setOnContinue(Runnable action) {
        continueButton.setOnAction(event -> action.run());
    }

    public void show() {
        root.setVisible(true);
        root.setManaged(true);
        root.toFront();
    }

    public void hide() {
        root.setVisible(false);
        root.setManaged(false);
    }
}