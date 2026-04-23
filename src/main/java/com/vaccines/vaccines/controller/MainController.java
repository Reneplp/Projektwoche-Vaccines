package com.vaccines.vaccines.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Alert;

public class MainController {

    @FXML
    private TextField nameField;

    @FXML
    private void handleWeiter() {
        String name = nameField.getText();

        if (name.isBlank()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Kein Name eingegeben");
            alert.setHeaderText(null);
            alert.setContentText("Bitte sag uns zuerst, wie du heißt, dann geht's weiter!");
            alert.showAndWait();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/vaccines/vaccines/profile-type-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 800, 600);


            ProfileTypeController controller = loader.getController();
            controller.setName(name);


            Stage stage = (Stage) nameField.getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            System.out.println("Fehler beim Laden des nächsten Screens");
        }
    }
}