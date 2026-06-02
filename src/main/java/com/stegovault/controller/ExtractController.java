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
import com.stegovault.util.ImageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Alert;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;

public class ExtractController {

    @FXML
    private TextField imagePathField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextArea resultArea;

    private final CryptoService crypto=new CryptoServiceImpl();
    private final ValidationService validation=new ValidationServiceImpl();
    private final HashService hash=new HashServiceImpl();
    private final StegoService stego= new StegoServiceImpl(crypto, validation, hash);

    public void onChooseImage(){
        System.out.println("choose image");

        FileChooser chooser = new FileChooser();

        chooser.setTitle("Choose Image");

        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.bmp"));

        File file = chooser.showOpenDialog(new Stage());

        if (file != null) {
            imagePathField.setText(file.getAbsolutePath());
        }
    }

    public void onExtract() {
        System.out.println("extract");

        try{
            Path imagePath= Path.of(imagePathField.getText());

            String password=passwordField.getText();

            BufferedImage image= ImageUtil.read(imagePath);

            EncryptionConfig config=new EncryptionConfig(password,new byte[16], new byte[16], 65536);

            String text=stego.decode(image, config);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("text extracted successfully.");

            alert.showAndWait();

            resultArea.setText(text);

            System.out.println("DECODE DOONE");
        }catch(Exception e){
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Error");
            alert.setHeaderText("Operation failed");
            alert.setContentText(e.getMessage());

            alert.showAndWait();
        }
    }

}
