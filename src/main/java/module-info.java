module cz.cvut.fel.pjv.warforpower {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.naming;
    requires org.slf4j;
    requires ch.qos.logback.classic;

    requires com.google.gson;
    opens cz.cvut.fel.pjv.warforpower.save to com.google.gson;


    opens cz.cvut.fel.pjv.warforpower to javafx.fxml;
    exports cz.cvut.fel.pjv.warforpower;
}