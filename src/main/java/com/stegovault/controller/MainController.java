package com.stegovault.controller;

import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.awt.*;

public class MainController {

    public void onEmbed(ActionEvent event)throws Exception{

        FXMLLoader loader=new FXMLLoader(getClass().getResource("/fxml/embed-view.fxml"));

        Scene scene=new Scene(loader.load());

        Stage stage=(Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setScene(scene);
    }

    public void onExtract(ActionEvent event) throws Exception{
        System.out.println("EXTRACT cliked");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/extract-view.fxml"));

        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        stage.setScene(scene);
    }
}
