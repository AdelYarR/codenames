package ru.myapp.semester_work.client.controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.myapp.semester_work.client.service.ClientService;
import ru.myapp.semester_work.models.PlayerType;
import ru.myapp.semester_work.models.Room;
import ru.myapp.semester_work.models.TeamType;
import ru.myapp.semester_work.models.WordData;
import ru.myapp.semester_work.utils.AlertHelper;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class NavigationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationController.class);

    private final ClientService clientService;
    private final StackPane container;

    private WelcomeController welcomeController;
    private HubController hubController;
    private GameSessionController gameSessionController;

    public NavigationController(ClientService clientService, StackPane container) {
        this.clientService = clientService;
        this.container = container;
    }

    public void showWelcome() {
        Platform.runLater(() ->
        {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("welcome-view.fxml"));
                loader.setControllerFactory(c -> {
                    welcomeController = new WelcomeController(clientService);
                    return welcomeController;
                });
                Parent root = loader.load();

                container.getChildren().clear();
                container.getChildren().add(root);
            } catch (IOException e) {
                LOGGER.error("Не удалось переключиться на окно входа.", e);
            }
        });
    }

    public void showHub() {
        Platform.runLater(() ->
        {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("hub-view.fxml"));
                loader.setControllerFactory(c -> {
                    hubController = new HubController(clientService);
                    return hubController;
                });
                Parent root = loader.load();

                container.getChildren().clear();
                container.getChildren().add(root);
            } catch (IOException e) {
                LOGGER.error("Не удалось переключиться на окно хаба.", e);
            }
        });
    }

    public void showGameSession() {
        Platform.runLater(() ->
        {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("game-session-view.fxml"));
                loader.setControllerFactory(c -> {
                    gameSessionController = new GameSessionController(clientService);
                    return gameSessionController;
                });
                Parent root = loader.load();

                container.getChildren().clear();
                container.getChildren().add(root);
            } catch (IOException e) {
                LOGGER.error("Не удалось переключиться на окно игровой сессии.", e);
            }
        });
    }

    public void listRooms(List<Room> rooms) {
        Platform.runLater(() ->
        {
            if (hubController == null) {
                AlertHelper.showErrorAlert("Не удалось загрузить комнаты.", "Окно хаба не найдено.");
                return;
            }

            hubController.updateRooms(rooms);
        });
    }

    public void setPlayerNameToLabel(String clientName, TeamType teamType, PlayerType playerType) {
        Platform.runLater(() ->
        {
            if (gameSessionController == null) {
                AlertHelper.showErrorAlert("Не удалось обновить тип игрока.", "Окно игровой сессии не найдено.");
                return;
            }
            gameSessionController.setPlayerNameToLabel(clientName, teamType, playerType);
        });
    }

    public void clearOldLabel(TeamType teamType, PlayerType playerType) {
        Platform.runLater(() ->
        {
            if (gameSessionController == null) {
                AlertHelper.showErrorAlert("Не удалось очистить тип.", "Окно игровой сессии не найдено.");
                return;
            }

            if (teamType == TeamType.NONE && playerType == PlayerType.NONE) {
                return;
            }

            gameSessionController.clearOldLabel(teamType, playerType);
        });
    }

    public void setCounters(int redCount, int blueCount) {
        Platform.runLater(() ->
        {
            if (gameSessionController == null) {
                AlertHelper.showErrorAlert("Не удалось очистить тип.", "Окно игровой сессии не найдено.");
                return;
            }

            if (redCount < 0 || redCount > 9 || blueCount < 0 || blueCount > 9) {
                AlertHelper.showErrorAlert("Не удалось установить счёт.", "Счёт принимает некорректное значение.");
                return;
            }

            gameSessionController.setCounters(redCount, blueCount);
        });
    }

    public void generateWordCards(List<WordData> words, PlayerType playerType) {
        Platform.runLater(() ->
        {
            if (gameSessionController == null) {
                AlertHelper.showErrorAlert("Не удалось очистить тип.", "Окно игровой сессии не найдено.");
                return;
            }

            gameSessionController.generateWordCards(words, playerType);
        });
    }

    public void setPlayerPermission(String currentTurnName, TeamType teamType, PlayerType playerType) {
        Platform.runLater(() ->
        {
            if (gameSessionController == null) {
                AlertHelper.showErrorAlert("Не удалось очистить тип.", "Окно игровой сессии не найдено.");
                return;
            }

            Optional<String> optionalClientName = clientService.getClientName();
            if (optionalClientName.isEmpty()) {
                AlertHelper.showErrorAlert("Не удалось получить имя клиента.", "Клиент не зарегистрирован.");
                return;
            }
            String clientName = optionalClientName.get();

            if (clientName.equals(currentTurnName)) {
                gameSessionController.doTurn(teamType, playerType);
            } else {
                gameSessionController.blockTurn();
            }
        });
    }

    public void addMessageToListView(String message, TeamType teamType) {
        Platform.runLater(() ->
        {
            if (gameSessionController == null) {
                AlertHelper.showErrorAlert("Не удалось очистить тип.", "Окно игровой сессии не найдено.");
                return;
            }

            if (teamType == TeamType.RED) {
                gameSessionController.addMessageToRedListView(message);
            } else {
                gameSessionController.addMessageToBlueListView(message);
            }
        });
    }
}
