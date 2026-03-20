package cz.cvut.fel.pjv.warforpower.view.game;

import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

public class GameView {
    private final StackPane root = new StackPane();

    public Parent getRoot() {
        return root;
    }

    public GameView() {
        Canvas canvas = new Canvas(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Image background = new Image(getClass().getResource("/img/game_field.png").toExternalForm());
        gc.drawImage(background, 0, 0, UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);

        this.root.getChildren().addAll(canvas);
    }
}
