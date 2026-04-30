package cz.cvut.fel.pjv.warforpower.view.game.menu;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Reusable popup menu asking the user to confirm or cancel an action.
 */
public class ConfirmationMenuView {
    private final VBox root;
    private final Label messageLabel;
    private final Button confirmButton;
    private final Button cancelButton;

    public ConfirmationMenuView() {
        this.root = new VBox(14);
        this.messageLabel = new Label();
        this.confirmButton = new Button("Yes");
        this.cancelButton = new Button("No");

        root.setAlignment(Pos.CENTER);
        root.setFillWidth(true);
        root.setPadding(new Insets(16));
        root.setPrefWidth(250);
        root.setVisible(false);
        root.setManaged(false);

        root.setStyle("""
            -fx-background-color: #e3d4aa;
            -fx-border-color: #8b6f47;
            -fx-border-width: 2;
            -fx-border-radius: 14;
            -fx-background-radius: 14;
        """);

        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(Double.MAX_VALUE);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setStyle("""
            -fx-text-fill: #2f2418;
            -fx-font-size: 18px;
            -fx-font-weight: bold;
        """);

        confirmButton.setPrefWidth(90);
        cancelButton.setPrefWidth(90);

        confirmButton.setCursor(Cursor.HAND);
        cancelButton.setCursor(Cursor.HAND);

        confirmButton.setStyle("""
            -fx-background-color: rgb(236, 224, 193);
            -fx-text-fill: rgb(68, 49, 34);
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-border-color: rgb(125, 100, 72);
            -fx-border-width: 1.5;
        """);

        cancelButton.setStyle("""
            -fx-background-color: rgb(236, 224, 193);
            -fx-text-fill: rgb(68, 49, 34);
            -fx-font-size: 16px;
            -fx-font-weight: bold;
            -fx-background-radius: 10;
            -fx-border-radius: 10;
            -fx-border-color: rgb(125, 100, 72);
            -fx-border-width: 1.5;
        """);

        root.getChildren().addAll(messageLabel, confirmButton, cancelButton);
    }

    public VBox getRoot() {
        return root;
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void setOnConfirm(Runnable action) {
        confirmButton.setOnAction(event -> action.run());
    }

    public void setOnCancel(Runnable action) {
        cancelButton.setOnAction(event -> action.run());
    }

    public void showAt(double x, double y) {
        root.setLayoutX(x);
        root.setLayoutY(y);
        root.setVisible(true);
        root.setManaged(true);
    }

    public void hide() {
        root.setVisible(false);
        root.setManaged(false);
    }
}