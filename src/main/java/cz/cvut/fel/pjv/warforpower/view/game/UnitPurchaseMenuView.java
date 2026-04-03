package cz.cvut.fel.pjv.warforpower.view.game;

import cz.cvut.fel.pjv.warforpower.model.units.UnitType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.util.EnumMap;
import java.util.Map;

public class UnitPurchaseMenuView {
    private final VBox root = new VBox(8);
    private final Map<UnitType, Button> buyButtons = new EnumMap<>(UnitType.class);

    public UnitPurchaseMenuView() {
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(12));
        root.setVisible(false);
        root.setManaged(false);

        root.setStyle(
                "-fx-background-color: rgba(235, 221, 180, 0.97);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #7a5a32;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10, 0.2, 0, 3);"
        );

        for (UnitType unitType : UnitType.values()) {
            Button button = new Button(unitType.name() + " - " + unitType.getPrice());
            button.setPrefWidth(180);
            button.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #3f2c1b;" +
                            "-fx-background-color: linear-gradient(to bottom, #f0ddb0, #d6b978);" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-color: #7a5a32;" +
                            "-fx-border-width: 1.5;" +
                            "-fx-cursor: hand;" +
                            "-fx-padding: 8 12 8 12;"
            );
            buyButtons.put(unitType, button);
            root.getChildren().add(button);
        }
    }

    public VBox getRoot() {
        return root;
    }

    public void show() {
        root.setVisible(true);
        root.setManaged(true);
    }

    public void hide() {
        root.setVisible(false);
        root.setManaged(false);
    }

    public Button getBuyButton(UnitType unitType) {
        return buyButtons.get(unitType);
    }
}