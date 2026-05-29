package com.stegovault.controller;

import com.stegovault.service.CryptoService;
import com.stegovault.service.HashService;
import com.stegovault.service.StegoService;
import com.stegovault.service.ValidationService;
import com.stegovault.service.impl.CryptoServiceImpl;
import com.stegovault.service.impl.HashServiceImpl;
import com.stegovault.service.impl.StegoServiceImpl;
import com.stegovault.service.impl.ValidationServiceImpl;
import com.sun.javafx.reflect.FieldUtil;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;

public class EmbededController {
    @FXML
    private TextField txtPathField;
    @FXML
    private TextField imagePathField;
    @FXML
    private PasswordField passwordField;

    private final CryptoService crypto=new CryptoServiceImpl();
    private final ValidationService validation=new ValidationServiceImpl();
    private final HashService hash=new HashServiceImpl();
    private final StegoService stego= new StegoServiceImpl(crypto, validation, hash);

    public void onChooseTXT(){
        System.out.println("choose txt");

        FileChooser chooser=new FileChooser();

        chooser.setTitle("choose TXT file");

        File file=chooser.showOpenDialog(new Stage());

        if (file != null) {
            txtPathField.setText(file.getAbsolutePath());
        }

    }

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

    public void onEncode() {

        System.out.println("ENCODE");
        System.out.println(txtPathField.getText());
        System.out.println(imagePathField.getText());
        System.out.println(passwordField.getText());

        Path txtPath= Path.of(txtPathField.getText());
        Path imagePath=Path.of(imagePathField.getText());
        String password=passwordField.getText();

        byte[] textBytes= FieldUtil
    }
}
