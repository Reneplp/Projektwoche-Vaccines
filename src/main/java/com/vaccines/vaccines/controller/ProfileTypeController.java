package com.vaccines.vaccines.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Alert;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class ProfileTypeController {
private String userName;
    @FXML
    private Label greetingLabel;

    @FXML
    private ToggleGroup profileTypeGroup;

    public void setName(String name) {
        this.userName = name;
        greetingLabel.setText("Hey " + name + "! Für wen möchtest du ein Profil anlegen?");
    }
    @FXML
    private void handleZurueck() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/vaccines/vaccines/main-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) greetingLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }
    }
    @FXML
    private void handleWeiter() {
        RadioButton selected = (RadioButton) profileTypeGroup.getSelectedToggle();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Kein Profil ausgewählt.");
            alert.setHeaderText(null);
            alert.setContentText("Bitte sag uns erst, für wen du das Profil anlegen möchtest.");
            alert.showAndWait();
            return;
        }

        String profileType = (String) selected.getUserData();
        System.out.println("Gewählter Typ: " + profileType);

        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/vaccines/vaccines/profile-view.fxml")
            );

            Scene scene = new Scene(loader.load(), 800, 600);

            ProfileController controller = loader.getController();
            controller.setName(userName);
            controller.setProfileType(profileType);

            Stage stage = (Stage) greetingLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
           // System.out.println("Fehler beim Laden des nächsten Screens");
            e.printStackTrace(); // TODO
        }
    }
}