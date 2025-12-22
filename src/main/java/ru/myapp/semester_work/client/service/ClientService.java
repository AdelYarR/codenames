package ru.myapp.semester_work.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.myapp.semester_work.models.ClientData;
import ru.myapp.semester_work.models.PlayerType;
import ru.myapp.semester_work.models.TeamType;
import ru.myapp.semester_work.models.WordData;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Optional;

public class ClientService {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientService.class);

    private MessageHandler messageHandler;
    private final NetworkService networkService;
    private boolean isRunning;
    private SocketChannel clientChannel;
    private ClientData clientData;

    public ClientService(NetworkService networkService) {
        this.networkService = networkService;
    }

    public void setMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public ClientData validateAndGet(String name, String address, String port) {
        if (name.isEmpty() || name.length() > 32) {
            throw new IllegalArgumentException("Получено некорректное значение. Имя не должно быть пустым и превышать 32 символов");
        }

        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            int inetPort = Integer.parseInt(port);

            if (inetPort < 0 || inetPort > 65535) {
                throw new IllegalArgumentException("Получено некорректное значение порта. Число должно быть в диапазоне 0 до 65535");
            }

            return new ClientData(name, inetAddress, inetPort);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Получено некорректное значение адреса. Введите текст в формате 93.184.216.34 или localhost.", e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Получено некорректное значение порта. Введите число.", e);
        }
    }

    public void registerClient(ClientData clientData) {
        this.clientData = clientData;
        InetSocketAddress socketAddress = new InetSocketAddress(clientData.address(), clientData.port());
        try {
            clientChannel = SocketChannel.open(socketAddress);
            clientChannel.configureBlocking(false);

            LOGGER.info("Клиентский сокет успешно открыт на {}:{}.", clientData.address(), clientData.port());

            startMessageListener(clientChannel);

            String rawMessage = "SET_CLIENT|" + clientData.name();
            networkService.sendMessage(clientChannel, rawMessage);
        } catch (IOException e) {
            throw new IllegalArgumentException("Не удалось подключиться к серверу.", e);
        }
    }

    private void startMessageListener(SocketChannel clientChannel) {
        Thread listenerThread = new Thread(() -> {
            isRunning = true;
            while (isRunning) {
                messageHandler.handleServerMessage(clientChannel);
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    public void getRooms() {
        String rawMessage = "GET_ROOMS|r";
        networkService.sendMessage(clientChannel, rawMessage);
    }

    public void createRoom(String roomName) {
        String rawMessage = "CREATE_ROOM|" + roomName;
        networkService.sendMessage(clientChannel, rawMessage);
    }

    public void joinRoom(String roomName) {
        String rawMessage = "JOIN_ROOM|" + roomName + "|" + clientData.name();
        networkService.sendMessage(clientChannel, rawMessage);
    }

    public Optional<String> getClientName() {
        if (clientData == null) {
            return Optional.empty();
        }

        return Optional.of(clientData.name());
    }

    public void joinTeam(TeamType teamType, PlayerType playerType) {
        String rawMessage = String.format("JOIN_TEAM|%s|%s|%s",
                clientData.name(), teamType, playerType);
        networkService.sendMessage(clientChannel, rawMessage);
    }

    public void startGameSession() {
        String rawMessage = "START_SESSION|" + clientData.name();
        networkService.sendMessage(clientChannel, rawMessage);
    }

    public void sendCardClick(WordData wordData) {
        String rawMessage = String.format("CLICK_CARD|%s|%s",
                wordData.getPositionX(), wordData.getPositionY());
        networkService.sendMessage(clientChannel, rawMessage);
    }

    public void sendAddMessage(String message, TeamType teamType) {
        String rawMessage = String.format("ADD_MESSAGE|%s|%s",
                message, teamType);
        networkService.sendMessage(clientChannel, rawMessage);
    }

    public void sendEndTurn(TeamType teamType, PlayerType playerType) {
        String rawMessage = String.format("END_TURN|%s|%s",
                teamType, playerType);
        networkService.sendMessage(clientChannel, rawMessage);
    }
}
