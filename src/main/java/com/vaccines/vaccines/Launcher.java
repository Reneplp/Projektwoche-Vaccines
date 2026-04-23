package com.vaccines.vaccines;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Launcher extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/vaccines/vaccines/main-view.fxml")
        );
        Scene scene = new Scene(loader.load(), 800, 600);
        stage.setTitle("Healthio");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}