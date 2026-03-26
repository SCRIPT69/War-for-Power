module cz.cvut.fel.pjv.warforpower {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;


    opens cz.cvut.fel.pjv.warforpower to javafx.fxml;
    exports cz.cvut.fel.pjv.warforpower;
}