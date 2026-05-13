package cz.cvut.fel.pjv.warforpower.view.menu;

import cz.cvut.fel.pjv.warforpower.model.game.Game;
import cz.cvut.fel.pjv.warforpower.save.SaveManager;
import cz.cvut.fel.pjv.warforpower.view.SceneManager;
import cz.cvut.fel.pjv.warforpower.view.UIConstants;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class MenuView {
    private final SceneManager sceneManager;
    private final StackPane root = new StackPane();
    private int selectedPlayersCount = 2;
    private final boolean hasSave;

    public MenuView(SceneManager sceneManager, boolean hasSave) {
        this.sceneManager = sceneManager;
        this.hasSave = hasSave;

        Canvas canvas = new Canvas(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Image background = new Image(getClass().getResource("/img/LOGO.jpg").toExternalForm());
        gc.drawImage(background, 0, 0, UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);

        VBox menu = this.createMenu();

        Region darkOverlay = new Region();
        darkOverlay.setPrefSize(UIConstants.WINDOW_WIDTH, UIConstants.WINDOW_HEIGHT);
        darkOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2);");

        this.root.getChildren().addAll(canvas, darkOverlay, menu);
    }

    private VBox createMenu() {
        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(300);
        menuBox.setMaxHeight(450);

        menuBox.getStyleClass().add("root-menu-panel");

        Label selectLabel = new Label("Select number of players");
        selectLabel.getStyleClass().add("menu-title-label");

        Label selectedLabel = new Label("Selected: 2 players");
        selectedLabel.getStyleClass().add("menu-secondary-label");

        Button twoPlayersButton = new Button("2 Players");
        twoPlayersButton.getStyleClass().add("player-button");

        Button threePlayersButton = new Button("3 Players");
        threePlayersButton.getStyleClass().add("player-button");

        Button fourPlayersButton = new Button("4 Players");
        fourPlayersButton.getStyleClass().add("player-button");

        Button startButton = new Button("Start Game");
        startButton.getStyleClass().add("primary-button");

        List<Button> playerButtons = List.of(twoPlayersButton, threePlayersButton, fourPlayersButton);
        updateSelectedPlayerButton(playerButtons, twoPlayersButton);

        twoPlayersButton.setOnAction(event -> {
            selectedPlayersCount = 2;
            selectedLabel.setText("Selected: 2 players");
            updateSelectedPlayerButton(playerButtons, twoPlayersButton);
        });

        threePlayersButton.setOnAction(event -> {
            selectedPlayersCount = 3;
            selectedLabel.setText("Selected: 3 players");
            updateSelectedPlayerButton(playerButtons, threePlayersButton);
        });

        fourPlayersButton.setOnAction(event -> {
            selectedPlayersCount = 4;
            selectedLabel.setText("Selected: 4 players");
            updateSelectedPlayerButton(playerButtons, fourPlayersButton);
        });

        startButton.setOnAction(event -> {
            this.sceneManager.openGameScene(this.selectedPlayersCount);
        });

        menuBox.getChildren().addAll(
                selectLabel,
                selectedLabel,
                twoPlayersButton,
                threePlayersButton,
                fourPlayersButton,
                startButton
        );

        if (hasSave) {
            Button continueButton = new Button("Continue");
            continueButton.getStyleClass().add("primary-button");

            continueButton.setOnAction(event -> {
                Game loadedGame = SaveManager.load();

                if (loadedGame != null) {
                    sceneManager.openGameScene(loadedGame);
                }
            });

            menuBox.getChildren().add(continueButton);
        }

        VBox.setMargin(startButton, new Insets(15, 0, 0, 0));

        return menuBox;
    }

    private void updateSelectedPlayerButton(List<Button> buttons, Button selectedButton) {
        for (Button button : buttons) {
            button.getStyleClass().remove("player-button-selected");
            if (button == selectedButton) {
                button.getStyleClass().add("player-button-selected");
            }
        }
    }

    public Parent getRoot() {
        return root;
    }
}
