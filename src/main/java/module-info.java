module com.mycompany.group_9_comp20081_app {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.base;
    requires java.sql; // added
    requires java.datatransfer;
    requires zip4j;
    requires javafx.base;
    requires jsch;

    opens com.mycompany.group_9_comp20081_app to javafx.fxml;
    exports com.mycompany.group_9_comp20081_app;
}
