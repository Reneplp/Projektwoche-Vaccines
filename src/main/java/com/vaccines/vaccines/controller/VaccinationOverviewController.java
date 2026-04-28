package com.vaccines.vaccines.controller;

import com.vaccines.vaccines.model.Profile;
import com.vaccines.vaccines.model.Vaccination;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.LocalDate;

public class VaccinationOverviewController {

    @FXML private Label profileNameLabel;
    @FXML private TableView<Vaccination> vaccinationTable;
    @FXML private TableColumn<Vaccination, String> nameColumn;
    @FXML private TableColumn<Vaccination, String> diseaseColumn;
    @FXML private TableColumn<Vaccination, LocalDate> lastColumn;
    @FXML private TableColumn<Vaccination, LocalDate> nextColumn;
    @FXML private TableColumn<Vaccination, String> statusColumn;

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
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                }
            }
        });
        nextColumn.setCellValueFactory(new PropertyValueFactory<>("nextDue"));
        nextColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                }
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