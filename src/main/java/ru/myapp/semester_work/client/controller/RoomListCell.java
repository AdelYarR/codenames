package ru.myapp.semester_work.client.controller;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import ru.myapp.semester_work.client.service.ClientService;
import ru.myapp.semester_work.models.Room;
import ru.myapp.semester_work.utils.AlertHelper;

public class RoomListCell extends ListCell<Room> {

    private final ClientService clientService;

    private final HBox container;
    private final Label roomNameLabel;
    private final Button joinButton;

    public RoomListCell(ClientService clientService) {
        super();
        this.clientService = clientService;

        roomNameLabel = new Label();
        roomNameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        joinButton = new Button("Войти");
        joinButton.setOnAction(this::onJoinButtonClick);

        container = new HBox(10);
        container.setPadding(new Insets(5));

        container.getChildren().addAll(roomNameLabel, joinButton);
    }

    protected void updateItem(Room room, boolean empty) {
        super.updateItem(room, empty);

        if (empty || room == null) {
            setText(null);
            setGraphic(null);
        } else {
            roomNameLabel.setText(room.getName());
            setText(null);
            setGraphic(container);
        }
    }

    private void onJoinButtonClick(ActionEvent actionEvent) {
        Room room = getItem();
        if (room == null) {
            AlertHelper.showErrorAlert("Не удалось войти в комнату.", "Комната не была найдена.");
            return;
        }

        clientService.joinRoom(room.getName());
    }
}
