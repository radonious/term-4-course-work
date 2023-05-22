package com.example.term4chat.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

/* Класс для запуска приложения */
public class MyApp extends Application {

    /* Запуск приложения */
    public static void main(String[] args) {
        launch();
    }

    /* Иницализация и отображение приложения */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MyApp.class.getResource("/com/example/term4chat/fxml/MyApp.fxml"));
        Pane pane = fxmlLoader.load();
        Scene scene = new Scene(pane);
        Controller mainController = fxmlLoader.getController();

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.setResizable(false);
        stage.setTitle("2ch Online Chat");
        stage.setScene(scene);
        stage.show();
    }
}