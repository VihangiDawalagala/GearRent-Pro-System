package com.gearrent;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/gearrent/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 480, 360);
        scene.getStylesheets().add(
                getClass().getResource("/com/gearrent/css/styles.css").toExternalForm());
        primaryStage.setTitle("GearRent Pro – Login");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
