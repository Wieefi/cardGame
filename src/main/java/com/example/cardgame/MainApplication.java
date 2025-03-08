package com.example.cardgame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("cardgame-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("♠ ♥ ♦ ♣ Card Game♠ ♥ ♦ ♣");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}