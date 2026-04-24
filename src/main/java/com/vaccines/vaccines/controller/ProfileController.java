package com.vaccines.vaccines.controller;

import com.vaccines.vaccines.model.Profile;
import com.vaccines.vaccines.model.Vaccination;
import com.vaccines.vaccines.service.StorageService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.vaccines.vaccines.service.VaccineService;
import com.vaccines.vaccines.service.VaccineRegistry;
import com.vaccines.vaccines.model.ProfileType;

public class ProfileController {

    private List<String> currentTradeNames = new ArrayList<>();

    @FXML
    private Label greetingLabel;

    @FXML
    private VBox vaccinationsBox;

    @FXML
    private VBox extraFieldsBox;

    @FXML
    private DatePicker birthDatePicker;

    @FXML
    private TextField subjectNameField;


    @FXML
    private void handleAddVaccination() {
        HBox row = new HBox(10);
        row.setAlignment(javafx.geometry.Pos.CENTER);

        ComboBox<String> dropdown = new ComboBox<>();
        dropdown.setPrefWidth(300);
        setupAutocomplete(dropdown, currentTradeNames);

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Datum der Impfung");

        Button deleteButton = new Button("✖");
        deleteButton.setOnAction(e -> vaccinationsBox.getChildren().remove(row));

        row.getChildren().addAll(dropdown, datePicker, deleteButton);
        vaccinationsBox.getChildren().add(row);
    }

    private String userName;
    private String profileType;

    // Autocomplete Combobox von KI generiert: TODO -> Muss geändert werden auf TextField mit ListView, Space bug
    private void setupAutocomplete(ComboBox<String> comboBox, List<String> allItems) {
        comboBox.setEditable(true);
        comboBox.getItems().addAll(allItems);

        comboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            // Auswahl clearen wenn User anfängt zu tippen
            if (comboBox.getSelectionModel().getSelectedItem() != null &&
                    !comboBox.getSelectionModel().getSelectedItem().equals(newVal)) {
                comboBox.getSelectionModel().clearSelection();
            }
            // Wenn eine Auswahl getroffen wurde, nicht filtern
            if (comboBox.getSelectionModel().getSelectedItem() != null &&
                    comboBox.getSelectionModel().getSelectedItem().equals(newVal)) {
                return;
            }

            if (newVal == null || newVal.isEmpty()) {
                comboBox.getItems().setAll(allItems);
                return;
            }

            String filter = newVal.toLowerCase();
            List<String> filtered = allItems.stream()
                    .filter(item -> item.toLowerCase().contains(filter))
                    .collect(Collectors.toList());

            comboBox.getItems().setAll(filtered);

            if (!filtered.isEmpty()) {
                comboBox.show();
            }
        });

        // Reset wenn Dropdown geschlossen wird ohne Auswahl
        comboBox.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (!isShowing && comboBox.getSelectionModel().getSelectedItem() == null) {
                comboBox.getItems().setAll(allItems);
            }
        });
        comboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                javafx.application.Platform.runLater(() -> {
                    comboBox.getEditor().positionCaret(comboBox.getEditor().getText().length());
                    comboBox.getEditor().selectAll();
                    comboBox.getEditor().deselect();
                });
            }
        });
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;

        switch (profileType) {
            case "ERWACHSENER" -> {
                greetingLabel.setText("Alles klar " + userName + "! Bitte gib deine Daten ein.");
                extraFieldsBox.setVisible(false);
                extraFieldsBox.setManaged(false);
            }
            case "KIND" -> greetingLabel.setText("Alles klar " + userName + "! Bitte gib die Daten deines Kindes ein.");

            case "HUND" -> greetingLabel.setText("Alles klar " + userName + "! Bitte gib die Daten deines Hundes ein.");
            case "KATZE" -> greetingLabel.setText("Alles klar " + userName + "! Bitte gib die Daten deiner Katze ein.");
        }
        VaccineService vaccineService = new VaccineService();
        VaccineRegistry.ProfileType type = VaccineRegistry.ProfileType.valueOf(profileType);
        List<String> tradeNames = vaccineService.getTradeNamesForProfile(type);
        this.currentTradeNames = tradeNames;
        handleAddVaccination();
    }

    @FXML
    private void handleZurueck() {
        try {
            FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/com/vaccines/vaccines/profile-type-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 800, 600);

            ProfileTypeController controller = loader.getController();
            controller.setName(userName);

            Stage stage = (Stage) greetingLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }
    }

    public void setName(String name) {
        this.userName = name;
    }
    @FXML
    private void handleSpeichern() {
        String nameForProfile = "";
        LocalDate dateOfBirth = birthDatePicker.getValue();

        if (profileType.equals("ERWACHSENER")) {
            nameForProfile = userName;
        }
        else {
            nameForProfile = subjectNameField.getText();
            if (nameForProfile.isBlank()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Kein Name eingegeben");
                alert.setHeaderText(null);
                alert.setContentText("Bitte sag uns zuerst den Namen, dann geht's weiter!");
                alert.showAndWait();
                return;
            }
        }

        Profile profile = new Profile(ProfileType.valueOf(profileType), nameForProfile);
        profile.setDateOfBirth(dateOfBirth);

        for (Node node : vaccinationsBox.getChildren()) {
            HBox row = (HBox) node;
            ComboBox<String> tradeNameDropdown = (ComboBox<String>) row.getChildren().get(0);
            String tradeName = tradeNameDropdown.getValue();
            DatePicker lastVaccination = (DatePicker) row.getChildren().get(1);

            VaccineRegistry.VaccineEntry allData = VaccineRegistry.findByTradeName(tradeName);
            if (allData == null) continue;
            String diseaseName = allData.getDiseaseName();
            String protectionDuration = allData.getProtectionDuration();
            int booster = allData.getBoosterAfterMonths();


            Vaccination v = new Vaccination(tradeName,diseaseName, protectionDuration, booster, lastVaccination.getValue());
            profile.getVaccines().add(v);
        }
        StorageService storageService = new StorageService();
        ArrayList<Profile> profiles = storageService.loadProfiles();
        profiles.add(profile);
        storageService.saveProfiles(profiles);
    }
}