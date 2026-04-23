package com.vaccines.vaccines.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.ComboBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.vaccines.vaccines.service.VaccineService;
import com.vaccines.vaccines.service.VaccineRegistry;


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

    @FXML
    private void handleSpeichern() {
        // TODO
    }

    private String userName;
    private String profileType;

    // Autocomplete Combobox von KI generiert:
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
    }

    public void setProfileType(String profileType) {
        this.profileType = profileType;

        switch (profileType) {
            case "MENSCH_ERWACHSENER" -> {
                greetingLabel.setText("Alles klar " + userName + "! Bitte gib deine Daten ein.");
                extraFieldsBox.setVisible(false);
                extraFieldsBox.setManaged(false);
            }
            case "MENSCH_KIND" -> greetingLabel.setText("Alles klar " + userName + "! Bitte gib die Daten deines Kindes ein.");

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
}