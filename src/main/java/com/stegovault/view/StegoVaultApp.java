package com.stegovault.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.scene.image.Image;

public class StegoVaultApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setTitle("StegoVault");
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/icons/StegoVaultIcon.png"))
        );
        stage.setScene(scene);
        Rectangle2D screen = Screen.getPrimary().getVisualBounds();
        double w = screen.getWidth()  * 0.7;
        double h = screen.getHeight() * 0.6;
        stage.setResizable(true);
        stage.setWidth(w);
        stage.setHeight(h);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}

