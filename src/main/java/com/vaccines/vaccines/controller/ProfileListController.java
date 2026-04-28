package com.vaccines.vaccines.controller;

import com.vaccines.vaccines.model.Profile;
import com.vaccines.vaccines.service.StorageService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.ArrayList;

public class ProfileListController {

    @FXML
    private ListView<String> profileListView;

    private ArrayList<Profile> profiles;

    @FXML
    public void initialize() {
        StorageService storageService = new StorageService();
        profiles = storageService.loadProfiles();

        for (Profile profile : profiles) {
            profileListView.getItems().add(profile.getName() + " (" + profile.getType() + ")");
        }
    }

    @FXML
    private void handleProfilOeffnen() {
        int selectedIndex = profileListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1) return;

        Profile selected = profiles.get(selectedIndex);

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/vaccines/vaccines/vaccination-overview-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 800, 600);

            VaccinationOverviewController controller = loader.getController();
            controller.setProfile(selected);

            Stage stage = (Stage) profileListView.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleZurueck() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/vaccines/vaccines/main-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) profileListView.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}