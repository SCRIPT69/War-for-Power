module cz.cvut.fel.pjv.warforpower {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;
    requires java.logging;


    opens cz.cvut.fel.pjv.warforpower to javafx.fxml;
    exports cz.cvut.fel.pjv.warforpower;
}