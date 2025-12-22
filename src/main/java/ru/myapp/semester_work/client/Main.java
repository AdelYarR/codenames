package ru.myapp.semester_work.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import ru.myapp.semester_work.client.controller.NavigationController;
import ru.myapp.semester_work.client.service.ClientService;
import ru.myapp.semester_work.client.service.MessageHandler;
import ru.myapp.semester_work.client.service.NetworkService;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {

        NetworkService networkService = new NetworkService();
        ClientService clientService = new ClientService(networkService);
        StackPane container = new StackPane();

        NavigationController navigationController = new NavigationController(clientService, container);
        navigationController.showWelcome();

        MessageHandler messageHandler = new MessageHandler(networkService, navigationController);
        clientService.setMessageHandler(messageHandler);

        Scene scene = new Scene(container, 800, 600);

        primaryStage.setTitle("Codenames");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}