package com.stegovault.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StegoVaultApp extends Application{

    @Override
    public void start(Stage stage)throws Exception{
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/main-view.fxml")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("StegoVault");
        stage.setScene(scene);
//        stage.setWidth(600);
//        stage.setHeight(500);
//        //stage.setResizable(false);
        stage.show();

    }

    public static void main(String[] args){
        launch();
    }

}
