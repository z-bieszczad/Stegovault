package com.stegovault.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import com.stegovault.util.FileUtil;
import com.stegovault.service.CryptoService;
import com.stegovault.service.HashService;
import com.stegovault.service.StegoService;
import com.stegovault.service.ValidationService;
import com.stegovault.service.impl.CryptoServiceImpl;
import com.stegovault.service.impl.HashServiceImpl;
import com.stegovault.service.impl.StegoServiceImpl;
import com.stegovault.service.impl.ValidationServiceImpl;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ExtractController {

    @FXML private VBox rootPane;
    @FXML private HBox imageDropZone;
    @FXML private TextField imagePathField;
    @FXML private TextField outputPathField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Label passwordHint;
    @FXML private ProgressBar progressBar;
    @FXML private TextArea outputArea;

    private final CryptoService crypto = new CryptoServiceImpl();
    private final ValidationService validation = new ValidationServiceImpl();
    private final HashService hash = new HashServiceImpl();

    private final StegoService stego = new StegoServiceImpl(crypto, validation, hash);

    @FXML
    public void initialize() {
        passwordField.textProperty().addListener((obs, old, val) -> {
            if (val.isEmpty()) {
                passwordHint.setText("Min 8 chars · uppercase · lowercase · digit");
                passwordHint.setStyle("");
            } else if (validation.validatePassword(val)) {
                passwordHint.setText("✔ Password valid");
                passwordHint.setStyle("-fx-text-fill: #4caf7d;");
            } else {
                passwordHint.setText("✘ Min 8 chars · uppercase · lowercase · digit");
                passwordHint.setStyle("-fx-text-fill: #e06c75;");
            }
        });

        setupDrop(imageDropZone, imagePathField, new String[]{".png", ".bmp"}, this::loadImageFile);
    }

    private void setupDrop(Node zone, TextField field, String[] extensions, Consumer<File> handler) {
        zone.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles() && isValidFile(e.getDragboard().getFiles().get(0), extensions)) {
                e.acceptTransferModes(TransferMode.COPY);
                if (!field.getStyleClass().contains("drop-target"))
                    field.getStyleClass().add("drop-target");
            }
            e.consume();
        });
        zone.setOnDragExited(e -> {
            field.getStyleClass().remove("drop-target");
            e.consume();
        });
        zone.setOnDragDropped(e -> {
            field.getStyleClass().remove("drop-target");
            boolean success = false;
            if (e.getDragboard().hasFiles()) {
                File file = e.getDragboard().getFiles().get(0);
                if (isValidFile(file, extensions)) {
                    handler.accept(file);
                    success = true;
                }
            }
            e.setDropCompleted(success);
            e.consume();
        });
    }

    private boolean isValidFile(File file, String[] extensions) {
        String name = file.getName().toLowerCase();
        for (String ext : extensions) {
            if (name.endsWith(ext)) return true;
        }
        return false;
    }

    public void onBack(ActionEvent event) throws Exception {
        MainController.loadView("/fxml/main-view.fxml", rootPane);
    }

    public void onChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.bmp")
        );
        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            loadImageFile(file);
        }
    }

    private void loadImageFile(File file) {
        imagePathField.setText(file.getAbsolutePath());
        try {
            BufferedImage img = ImageIO.read(file);
            setStatus("Image loaded: " + img.getWidth() + "×" + img.getHeight(), false);
        } catch (IOException e) {
            setStatus("Could not read image.", true);
        }
    }

    public void onExtract() {
        String imagePath = imagePathField.getText().trim();
        String password = passwordField.getText();

        if (imagePath.isEmpty()) {
            setStatus("Please select an image.", true);
            return;
        }
        if (!validation.validatePassword(password)) {
            setStatus("Invalid password.", true);
            return;
        }

        progressBar.setVisible(true);
        progressBar.setManaged(true);
        progressBar.setProgress(-1);

        new Thread(() -> {
            try {
                BufferedImage image = ImageIO.read(Path.of(imagePath).toFile());
                String extracted = stego.decode(image, password);

                Platform.runLater(() -> {
                    progressBar.setProgress(1.0);
                    outputArea.setText(extracted);
                    String outPath = outputPathField.getText().trim();
                    if (!outPath.isEmpty()) {
                        saveToFile(extracted, outPath);
                    } else {
                        setStatus("✔ Message extracted successfully", false);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    progressBar.setManaged(false);
                    setStatus("Error: " + e.getMessage(), true);
                });
            }
        }).start();
    }

    public void onChooseOutputPath() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save extracted text as");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        File file = chooser.showSaveDialog(getStage());
        if (file != null) {
            outputPathField.setText(file.getAbsolutePath());
        }
    }

    public void onSaveResult() {
        String text = outputArea.getText();
        String path = outputPathField.getText().trim();
        if (text == null || text.isEmpty()) {
            setStatus("Nothing to save.", true);
            return;
        }
        if (path.isEmpty()) {
            setStatus("No output file selected.", true);
            return;
        }
        saveToFile(text, path);
    }

    private void saveToFile(String text, String path) {
        try {
            FileUtil.writeText(text, Path.of(path));
            setStatus("✔ Saved to: " + path, false);
        } catch (IOException e) {
            setStatus("Error saving file: " + e.getMessage(), true);
        }
    }

    public void onCopyResult() {
        String text = outputArea.getText();
        if (text == null || text.isEmpty()) return;

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);

        setStatus("Copied to clipboard.", false);
    }

    private void setStatus(String message, boolean error) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-ok", "status-err");
        statusLabel.getStyleClass().add(error ? "status-err" : "status-ok");
    }

    private Stage getStage() {
        return (Stage) imagePathField.getScene().getWindow();
    }
}
