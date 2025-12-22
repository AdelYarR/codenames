package ru.myapp.semester_work.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AlertHelper {

    private AlertHelper() {}

    public static void showErrorAlert(String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);

        alert.setTitle("Ошибка");
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.showAndWait();
    }
}
