package com.vaccines.vaccines.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class MainController {

    @FXML
    private TextField nameField;

    @FXML
    private void handleWeiter() {
        String name = nameField.getText();
        System.out.println("Name eingegeben: " + name);
    }
}