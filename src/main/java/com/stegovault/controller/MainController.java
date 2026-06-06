package com.stegovault.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainController {

    @FXML private VBox rootPane;
    @FXML private Button themeBtn;

    private static boolean darkMode = true;

    @FXML
    public void initialize() {
        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) applyTheme();
        });
    }

    public void onEmbed(MouseEvent event) throws Exception {
        loadView("/fxml/embed-view.fxml", (Node) event.getSource());
    }

    public void onExtract(MouseEvent event) throws Exception {
        loadView("/fxml/extract-view.fxml", (Node) event.getSource());
    }

    public void onToggleTheme(ActionEvent event) {
        darkMode = !darkMode;
        applyTheme();
    }

    private void applyTheme() {
        if (rootPane == null) return;
        Scene scene = rootPane.getScene();
        if (scene == null) return;
        if (darkMode) {
            scene.getRoot().getStyleClass().remove("light-mode");
            themeBtn.setText("Light mode");
        } else {
            if (!scene.getRoot().getStyleClass().contains("light-mode"))
                scene.getRoot().getStyleClass().add("light-mode");
            themeBtn.setText("Dark mode");
        }
    }

    static void applyThemeToScene(Scene scene) {
        if (darkMode) {
            scene.getRoot().getStyleClass().remove("light-mode");
        } else {
            if (!scene.getRoot().getStyleClass().contains("light-mode"))
                scene.getRoot().getStyleClass().add("light-mode");
        }
    }

    static void loadView(String fxml, Node source) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainController.class.getResource(fxml));
        Stage stage = (Stage) source.getScene().getWindow();

        double w = stage.getWidth();
        double h = stage.getHeight();

        Scene scene = new Scene(loader.load(), w, h);
        scene.getStylesheets().addAll(source.getScene().getStylesheets());
        applyThemeToScene(scene);
        stage.setScene(scene);
    }

    public static boolean isDarkMode() { return darkMode; }
    public static void setDarkMode(boolean v) { darkMode = v; }
}