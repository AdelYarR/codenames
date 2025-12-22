package ru.myapp.semester_work.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import ru.myapp.semester_work.models.ClientData;
import ru.myapp.semester_work.client.service.ClientService;
import ru.myapp.semester_work.utils.AlertHelper;

public class WelcomeController {

    private final ClientService clientService;

    public WelcomeController(ClientService clientService) {
        this.clientService = clientService;
    }

    @FXML
    private TextField nameField;

    @FXML
    private TextField addressField;

    @FXML
    private TextField portField;

    @FXML
    private void onEnterButtonClick() {
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        String port = portField.getText().trim();

        try {
            ClientData clientData = clientService.validateAndGet(name, address, port);
            clientService.registerClient(clientData);
        } catch (Exception e) {
            AlertHelper.showErrorAlert("Не удалось войти", e.toString());
        }
    }
}
