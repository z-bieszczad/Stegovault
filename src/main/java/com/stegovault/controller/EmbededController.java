package com.stegovault.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class EmbededController {

    @FXML private VBox rootPane;
    @FXML private HBox txtDropZone;
    @FXML private HBox imageDropZone;
    @FXML private TextField txtPathField;
    @FXML private TextField imagePathField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;
    @FXML private Label passwordHint;
    @FXML private ProgressBar capacityBar;
    @FXML private Label capacityLabel;
    @FXML private HBox capacityRow;

    private final CryptoService crypto = new CryptoServiceImpl();
    private final ValidationService validation = new ValidationServiceImpl();
    private final HashService hash = new HashServiceImpl();
    private final StegoService stego = new StegoServiceImpl(crypto, validation, hash);

    private BufferedImage loadedImage = null;
    private Image originalImage = null;
    private long loadedTextBytes = -1;

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

        setupDrop(txtDropZone, txtPathField, new String[]{".txt"}, file -> {
            txtPathField.setText(file.getAbsolutePath());
            loadedTextBytes = file.length();
            updateCapacity();
            setStatus("TXT file selected.", false);
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

    public void onChooseTXT() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose TXT file");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            txtPathField.setText(file.getAbsolutePath());
            loadedTextBytes = file.length();
            updateCapacity();
            setStatus("TXT file selected.", false);
        }
    }

    public void onChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.bmp"));
        File file = chooser.showOpenDialog(getStage());
        if (file != null) {
            loadImageFile(file);
        }
    }

    private void loadImageFile(File file) {
        imagePathField.setText(file.getAbsolutePath());
        try {
            loadedImage = ImageIO.read(file);
            originalImage = new Image(file.toURI().toString());
            updateCapacity();
            setStatus("Image loaded: " + loadedImage.getWidth() + "×" + loadedImage.getHeight(), false);
        } catch (Exception e) {
            setStatus("Could not read image.", true);
            loadedImage = null;
        }
    }

    private void updateCapacity() {
        if (loadedImage == null || loadedTextBytes < 0) return;

        int availableBits = loadedImage.getWidth() * loadedImage.getHeight() * 3;
        // AES-CBC pads text to the next 16-byte block; payload header overhead is 68 bytes
        long aesPaddedBytes = (loadedTextBytes / 16 + 1) * 16;
        long requiredBits = (4 + 16 + 16 + 32 + aesPaddedBytes) * 8;

        double ratio = (double) requiredBits / availableBits;
        capacityBar.setProgress(Math.min(ratio, 1.0));
        capacityLabel.setText(Math.round(ratio * 100) + "%");

        capacityBar.getStyleClass().removeAll("capacity-ok", "capacity-warn", "capacity-full");
        if (ratio > 1.0) {
            capacityBar.getStyleClass().add("capacity-full");
        } else if (ratio > 0.8) {
            capacityBar.getStyleClass().add("capacity-warn");
        } else {
            capacityBar.getStyleClass().add("capacity-ok");
        }

        capacityRow.setVisible(true);
        capacityRow.setManaged(true);
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
                    Image encodedImg = new Image(outputPath.toUri().toString());
                    setStatus("✔ Saved to: " + outputPath, false);
                    showComparisonWindow(originalImage, encodedImg);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
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

    private void showComparisonWindow(Image original, Image encoded) {
        ImageView origView = new ImageView(original);
        origView.setFitWidth(480);
        origView.setFitHeight(480);
        origView.setPreserveRatio(true);

        ImageView encView = new ImageView(encoded);
        encView.setFitWidth(480);
        encView.setFitHeight(480);
        encView.setPreserveRatio(true);

        Label origLabel = new Label("ORIGINAL");
        origLabel.getStyleClass().add("label-section");

        Label encLabel = new Label("ENCODED");
        encLabel.getStyleClass().add("label-section");

        VBox origBox = new VBox(8, origLabel, origView);
        origBox.setAlignment(Pos.TOP_CENTER);

        VBox encBox = new VBox(8, encLabel, encView);
        encBox.setAlignment(Pos.TOP_CENTER);

        Separator sep = new Separator(javafx.geometry.Orientation.VERTICAL);

        HBox root = new HBox(24, origBox, sep, encBox);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));

        Scene scene = new Scene(root);
        scene.getStylesheets().addAll(getStage().getScene().getStylesheets());

        Stage stage = new Stage();
        stage.setTitle("Image Comparison");
        stage.setScene(scene);
        stage.initOwner(getStage());
        stage.show();
    }
}
