module ru.myapp.semester_work.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.slf4j;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;

    opens ru.myapp.semester_work.client to javafx.fxml;
    opens ru.myapp.semester_work.client.controller to javafx.fxml;
    opens ru.myapp.semester_work.client.service to javafx.fxml;
    opens ru.myapp.semester_work.models to javafx.fxml;

    exports ru.myapp.semester_work.client;
    exports ru.myapp.semester_work.client.controller;
    exports ru.myapp.semester_work.client.service;
    exports ru.myapp.semester_work.models;

    exports ru.myapp.semester_work.utils;
    opens ru.myapp.semester_work.utils to javafx.fxml;
}