package com.vaccines.vaccines.controller;

import com.vaccines.vaccines.model.Profile;
import com.vaccines.vaccines.model.ProfileType;
import com.vaccines.vaccines.model.Vaccination;
import com.vaccines.vaccines.service.VaccineRegistry;
import com.vaccines.vaccines.service.VaccineService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import com.vaccines.vaccines.service.StorageService;
import java.util.ArrayList;

public class VaccinationOverviewController {

    @FXML private Label profileNameLabel;
    @FXML private TableView<Vaccination> vaccinationTable;
    @FXML private TableColumn<Vaccination, String> nameColumn;
    @FXML private TableColumn<Vaccination, String> diseaseColumn;
    @FXML private TableColumn<Vaccination, LocalDate> lastColumn;
    @FXML private TableColumn<Vaccination, LocalDate> nextColumn;
    @FXML private TableColumn<Vaccination, String> statusColumn;
    @FXML private TableColumn<Vaccination, Void> deleteColumn;

    private Profile profile;

    public void setProfile(Profile profile) {
        this.profile = profile;
        profileNameLabel.setText("Impfübersicht: " + profile.getName());

        nameColumn.setCellValueFactory(new PropertyValueFactory<>("tradeName"));
        diseaseColumn.setCellValueFactory(new PropertyValueFactory<>("diseaseName"));

        lastColumn.setCellValueFactory(new PropertyValueFactory<>("lastVaccination"));
        lastColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) setText(null);
                else setText(date.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            }
        });

        nextColumn.setCellValueFactory(new PropertyValueFactory<>("nextDue"));
        nextColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) setText(null);
                else setText(date.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            }
        });

        statusColumn.setCellValueFactory(cellData -> {
            LocalDate nextDue = cellData.getValue().getNextDue();
            LocalDate today = LocalDate.now();
            if (nextDue == null) return new javafx.beans.property.SimpleStringProperty("Unbekannt");
            long daysUntil = today.until(nextDue, java.time.temporal.ChronoUnit.DAYS);
            if (daysUntil < 0) return new javafx.beans.property.SimpleStringProperty("⚠ Überfällig");
            if (daysUntil < 90) return new javafx.beans.property.SimpleStringProperty("⚡ Bald fällig");
            return new javafx.beans.property.SimpleStringProperty("✓ OK");
        });

        vaccinationTable.getItems().addAll(profile.getVaccines());

        deleteColumn.setCellFactory(column -> new TableCell<>() {
            final Button deleteButton = new Button("✖");

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                    deleteButton.setOnAction(e -> {
                        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                        confirm.setTitle("Impfung löschen");
                        confirm.setHeaderText(null);
                        confirm.setContentText("Möchtest du diese Impfung wirklich löschen?");
                        confirm.showAndWait().ifPresent(result -> {
                            if (result == ButtonType.OK) {
                                Vaccination v = getTableView().getItems().get(getIndex());
                                profile.getVaccines().remove(v);
                                StorageService storageService = new StorageService();
                                ArrayList<Profile> profiles = storageService.loadProfiles();
                                for (int i = 0; i < profiles.size(); i++) {
                                    if (profiles.get(i).getName().equals(profile.getName())) {
                                        profiles.set(i, profile);
                                        break;
                                    }
                                }
                                storageService.saveProfiles(profiles);
                                vaccinationTable.getItems().remove(v);
                            }
                        });
                    });
                }
            }
        });
    }

    @FXML
    private void handleImpfungHinzufuegen() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Impfung hinzufügen");
        dialog.setHeaderText("Neue Impfung für " + profile.getName());

        ButtonType speichernButton = new ButtonType("Speichern", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(speichernButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        ComboBox<String> dropdown = new ComboBox<>();
        dropdown.setEditable(true);
        dropdown.setPrefWidth(300);

        VaccineService vaccineService = new VaccineService();
        ProfileType type = ProfileType.valueOf(profile.getType().toString());
        dropdown.getItems().addAll(vaccineService.getTradeNamesForProfile(type));
        dropdown.setPromptText("Impfstoff suchen...");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Datum der Impfung");

        content.getChildren().addAll(
                new Label("Impfstoff:"), dropdown,
                new Label("Datum der Impfung:"), datePicker
        );
        dialog.getDialogPane().setContent(content);

        dialog.showAndWait().ifPresent(result -> {
            if (result == speichernButton) {
                String tradeName = dropdown.getValue();
                LocalDate date = datePicker.getValue();
                if (tradeName == null || date == null) return;

                VaccineRegistry.VaccineEntry entry = VaccineRegistry.findByTradeName(tradeName);
                if (entry == null) return;

                Vaccination v = new Vaccination(tradeName, entry.getDiseaseName(),
                        entry.getProtectionDuration(), entry.getBoosterAfterMonths(), date);
                profile.getVaccines().add(v);

                StorageService storageService = new StorageService();
                ArrayList<Profile> profiles = storageService.loadProfiles();
                for (int i = 0; i < profiles.size(); i++) {
                    if (profiles.get(i).getName().equals(profile.getName())) {
                        profiles.set(i, profile);
                        break;
                    }
                }
                storageService.saveProfiles(profiles);
                vaccinationTable.getItems().clear();
                vaccinationTable.getItems().addAll(profile.getVaccines());
            }
        });
    }

    @FXML
    private void handleProfilLoeschen() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Profil löschen");
        confirm.setHeaderText(null);
        confirm.setContentText("Möchtest du das Profil von " + profile.getName() + " wirklich löschen? Diese Aktion kann nicht rückgängig gemacht werden.");

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                StorageService storageService = new StorageService();
                ArrayList<Profile> profiles = storageService.loadProfiles();
                profiles.removeIf(p -> p.getName().equals(profile.getName()));
                storageService.saveProfiles(profiles);

                try {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource("/com/vaccines/vaccines/profile-list-view.fxml")
                    );
                    Scene scene = new Scene(loader.load(), 800, 600);
                    Stage stage = (Stage) profileNameLabel.getScene().getWindow();
                    stage.setScene(scene);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleZurueck() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/vaccines/vaccines/profile-list-view.fxml")
            );
            Scene scene = new Scene(loader.load(), 800, 600);
            Stage stage = (Stage) profileNameLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}