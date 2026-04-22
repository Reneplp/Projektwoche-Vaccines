module com.vaccines.vaccines {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.vaccines.vaccines.controller to javafx.fxml;
    exports com.vaccines.vaccines;
}