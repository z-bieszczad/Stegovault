package com.stegovault.controller;

import com.stegovault.model.EncryptionConfig;
import com.stegovault.service.CryptoService;
import com.stegovault.service.HashService;
import com.stegovault.service.StegoService;
import com.stegovault.service.ValidationService;
import com.stegovault.service.impl.CryptoServiceImpl;
import com.stegovault.service.impl.HashServiceImpl;
import com.stegovault.service.impl.StegoServiceImpl;
import com.stegovault.service.impl.ValidationServiceImpl;
import com.stegovault.util.CryptoUtil;
import com.stegovault.util.FileUtil;
import com.stegovault.util.ImageUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

public class EmbededController {

    @FXML private VBox rootPane;
    @FXML private TextField txtPathField;
    @FXML private TextField imagePathField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Label passwordHint;
    @FXML private ProgressBar progressBar;
    @FXML private ProgressBar capacityBar;
    @FXML private Label capacityLabel;
    @FXML private HBox capacityRow;

    private final CryptoService crypto = new CryptoServiceImpl();
    private final ValidationService validation = new ValidationServiceImpl();
    private final HashService hash = new HashServiceImpl();
    private final StegoService stego = new StegoServiceImpl(crypto, validation, hash);

    private BufferedImage loadedImage = null;

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
    }

    public void onBack(ActionEvent event) throws Exception {
        MainController.loadView("/fxml/main-view.fxml", rootPane);
    }

    public void onChooseTXT() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose TXT file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            txtPathField.setText(file.getAbsolutePath());
            // updateCapacity();
            setStatus("TXT file selected.", false);
        }
    }

    public void onChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.bmp"));
        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            imagePathField.setText(file.getAbsolutePath());
            try {
                loadedImage = ImageIO.read(file);
                // updateCapacity();
                setStatus("Image loaded: " + loadedImage.getWidth() + "×" + loadedImage.getHeight(), false);
            } catch (Exception e) {
                setStatus("Could not read image.", true);
                loadedImage = null;
            }
        }
    }

    public void onEncode() {
        String txtPath = txtPathField.getText().trim();
        String imagePath = imagePathField.getText().trim();
        String password = passwordField.getText();

        if (txtPath.isEmpty() || imagePath.isEmpty()) {
            setStatus("Please select both a TXT file and an image.", true);
            return;
        }
        if (!validation.validatePassword(password)) {
            setStatus("Invalid password. Min 8 chars, uppercase, lowercase, digit.", true);
            return;
        }

        progressBar.setVisible(true);
        progressBar.setManaged(true);
        progressBar.setProgress(-1);

        new Thread(() -> {
            try {
                String text = new String(FileUtil.read(Path.of(txtPath)));
                BufferedImage image = ImageUtil.read(Path.of(imagePath));

                byte[] salt = CryptoUtil.generateSalt();
                byte[] iv = CryptoUtil.generateIV();
                EncryptionConfig config = new EncryptionConfig(password, salt, iv, 100_000);

                BufferedImage encoded = stego.encode(text, config, image);

                Path outputPath = Path.of(imagePath).getParent().resolve("output.png");
                ImageUtil.writePNG(encoded, outputPath);

                Platform.runLater(() -> {
                    progressBar.setProgress(1.0);
                    setStatus("✔ Saved to: " + outputPath, false);
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Message embedded successfully.\nSaved to: " + outputPath);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    progressBar.setManaged(false);
                    setStatus("Error: " + e.getMessage(), true);
                    showAlert(Alert.AlertType.ERROR, "Error", e.getMessage());
                });
            }
        }).start();
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("status-ok", "status-err");
        statusLabel.getStyleClass().add(isError ? "status-err" : "status-ok");
    }

    private Stage getStage() {
        return (Stage) txtPathField.getScene().getWindow();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}