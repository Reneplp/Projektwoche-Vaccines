module com.vaccines.vaccines {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.vaccines.vaccines to javafx.fxml;
    exports com.vaccines.vaccines;
}