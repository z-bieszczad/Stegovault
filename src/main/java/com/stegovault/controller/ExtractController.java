
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
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class ExtractController {

    @FXML private VBox rootPane;
    @FXML private TextField imagePathField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Label passwordHint;
    @FXML private ProgressBar progressBar;
    @FXML private TextArea outputArea;

    private final CryptoService crypto = new CryptoServiceImpl();
    private final ValidationService validation = new ValidationServiceImpl();
    private final HashService hash = new HashServiceImpl();

    private final StegoService stego =
            new StegoServiceImpl(crypto, validation, hash);

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

//        rootPane.setOnDragDropped(event->{ if( event.getGestureSource() !=rootPane && event.getDragboard().hasFiles()){
//             event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);   }
//            event.consume();
//        });
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
            imagePathField.setText(file.getAbsolutePath());
            try {
                loadedImage = ImageIO.read(file);
                setStatus("Image loaded: " +
                        loadedImage.getWidth() + "×" +
                        loadedImage.getHeight(), false);
            } catch (Exception e) {
                setStatus("Could not read image.", true);
                loadedImage = null;
            }
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
                    setStatus("✔ Message extracted successfully", false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    e.printStackTrace();
                    progressBar.setVisible(false);
                    progressBar.setManaged(false);
                    setStatus("Error: " + e.getMessage(), true);
                });
            }
        }).start();
    }


    public void onCopyResult() {
        String text = outputArea.getText();

        if (text == null || text.isEmpty()) return;

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);

        setStatus("copied to clipboard ", false);
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