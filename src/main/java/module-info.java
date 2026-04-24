module com.vaccines.vaccines {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.vaccines.vaccines.controller to javafx.fxml;
    opens com.vaccines.vaccines.model to com.google.gson;
    exports com.vaccines.vaccines;
}