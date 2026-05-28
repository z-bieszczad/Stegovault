package com.stegovault.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;

public class EmbededController {
    @FXML
    private TextField txtPathField;
    @FXML
    private TextField imagePathField;
    @FXML
    private PasswordField passwordField;

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
    }

    public void onEncode() {
        System.out.println("ENCODE");
    }
}
