package ru.myapp.semester_work.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import ru.myapp.semester_work.client.service.ClientService;
import ru.myapp.semester_work.models.Room;

import java.util.List;

public class HubController {

    private final ClientService clientService;

    public HubController(ClientService clientService) {
        this.clientService = clientService;
    }

    @FXML
    private ListView<Room> roomsListView;

    @FXML
    private TextField roomNameField;

    @FXML
    private void initialize() {
        roomsListView.setCellFactory(param -> new RoomListCell(clientService));

        loadRooms();
    }

    private void loadRooms() {
        clientService.getRooms();
    }

    public void updateRooms(List<Room> rooms) {
        roomsListView.getItems().setAll(rooms);
    }

    @FXML
    public void onCreateRoomButtonClick() {
        String roomName = roomNameField.getText().trim();
        if (!roomName.isEmpty()) {
            clientService.createRoom(roomName);
            roomNameField.clear();
        }
    }
}
