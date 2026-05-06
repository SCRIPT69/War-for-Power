package cz.cvut.fel.pjv.warforpower.view.game.battle;

import cz.cvut.fel.pjv.warforpower.model.units.Unit;
import cz.cvut.fel.pjv.warforpower.view.PlayerColorCssMapper;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Visual block representing one battle side.
 */
public class BattleSidePaneView {
    private final VBox root = new VBox(12);
    private final Label titleLabel = new Label();
    private final VBox unitsBox = new VBox(8);
    private final Label footerLabel = new Label();

    public BattleSidePaneView() {
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(220);

        titleLabel.setStyle("""
                -fx-font-size: 24px;
                -fx-font-weight: bold;
                -fx-text-fill: #3f2c1b;
                """);

        unitsBox.setAlignment(Pos.CENTER);

        root.getChildren().addAll(titleLabel, unitsBox);
    }

    public VBox getRoot() {
        return root;
    }

    /**
     * Updates displayed side information.
     *
     * @param title side title
     * @param units side units
     * @param isCityDefender true if this side represents a city defender
     */
    public void update(String title, List<Unit> units, boolean isCityDefender) {
        titleLabel.setText(title);
        unitsBox.getChildren().clear();

        if (isCityDefender && units.isEmpty()) {
            Label cityLabel = new Label("City");
            cityLabel.setStyle("""
                -fx-font-size: 22px;
                -fx-font-weight: bold;
                -fx-text-fill: #7a1f1f;
                """);
            unitsBox.getChildren().add(cityLabel);
            return;
        }

        for (Unit unit : units) {
            String colorCss = PlayerColorCssMapper.toCssColor(unit.getOwner().getColor());

            Label unitLabel = new Label(unit.getUnitType().name());
            unitLabel.setStyle("""
                -fx-font-size: 18px;
                -fx-font-weight: bold;
                -fx-text-fill: """ + colorCss + ";");

            unitsBox.getChildren().add(unitLabel);
        }
    }
}