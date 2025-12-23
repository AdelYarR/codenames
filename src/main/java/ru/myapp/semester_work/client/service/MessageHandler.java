package ru.myapp.semester_work.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.myapp.semester_work.client.controller.NavigationController;
import ru.myapp.semester_work.models.*;
import ru.myapp.semester_work.utils.MessageParser;
import ru.myapp.semester_work.utils.WordDataHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);
    private static final int BUFFER_SIZE = 1024;
    private static final String MESSAGE_DELIMITER = "\n";

    private final NetworkService networkService;
    private final NavigationController callback;
    private StringBuilder messageBuffer = new StringBuilder();

    public MessageHandler(NetworkService networkService, NavigationController callback) {
        this.networkService = networkService;
        this.callback = callback;
    }

    public void handleServerMessage(SocketChannel clientChannel) {
        List<String> messages = readMessages(clientChannel);

        for (String rawMessage : messages) {
            if (rawMessage == null || rawMessage.isEmpty()) {
                continue;
            }

            LOGGER.info("Получено сообщение от сервера: {}", rawMessage);

            try {
                Message message = MessageParser.parse(rawMessage);
                processMessage(clientChannel, message);
            } catch (IllegalArgumentException e) {
                LOGGER.error("Ошибка парсинга сообщения: '{}'", rawMessage, e);
            }
        }
    }

    private List<String> readMessages(SocketChannel clientChannel) {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        List<String> messages = new ArrayList<>();

        try {
            int read = clientChannel.read(buffer);
            if (read == -1) {
                throw new IOException("Доступ к серверу закрыт.");
            }

            if (read > 0) {
                buffer.flip();
                String received = StandardCharsets.UTF_8.decode(buffer).toString();
                messageBuffer.append(received);

                String bufferContent = messageBuffer.toString();
                String[] messageArray = bufferContent.split(MESSAGE_DELIMITER, -1);

                for (int i = 0; i < messageArray.length - 1; i++) {
                    String message = messageArray[i].trim();
                    if (!message.isEmpty()) {
                        messages.add(message);
                    }
                }

                messageBuffer = new StringBuilder(messageArray[messageArray.length - 1]);
            }
        } catch (IOException e) {
            LOGGER.error("Ошибка чтения сообщения", e);
            return messages;
        }

        return messages;
    }


    private void processMessage(SocketChannel clientChannel, Message message) {
        switch (message.getType()) {
            case CLIENT_ADDED:
                handleClientAdded(clientChannel);
                break;
            case ROOMS_GOT:
                handleRoomsGot(message.getPayload());
                break;
            case ROOM_JOINED:
                handleRoomJoined(clientChannel, message.getPayload());
                break;
            case TEAM_JOINED:
                handleTeamJoined(clientChannel, message.getPayload());
                break;
            case SESSION_STARTED:
                handleSessionStarted(clientChannel, message.getPayload());
                break;
            case MESSAGE_ADDED:
                handleMessageAdded(clientChannel, message.getPayload());
                break;
            case NEXT_TURN:
                handleNextTurn(clientChannel, message.getPayload());
                break;
            case CARD_CLICKED:
                handleCardClicked(clientChannel, message.getPayload());
                break;
        }
    }

    private void handleClientAdded(SocketChannel clientChannel) {
        callback.showHub();
        String rawMessage = "GET_ROOMS|r";
        networkService.sendMessage(clientChannel, rawMessage);
    }

    private void handleRoomsGot(List<String> payload) {
        List<Room> rooms = payload.stream()
                .map(Room::new)
                .toList();
        callback.listRooms(rooms);
    }

    private void handleRoomJoined(SocketChannel clientChannel, List<String> payload) {
        callback.showGameSession();
    }

    private void handleTeamJoined(SocketChannel clientChannel, List<String> payload) {
        String clientName = payload.get(0);
        TeamType teamType = TeamType.valueOf(payload.get(1));
        PlayerType playerType = PlayerType.valueOf(payload.get(2));
        TeamType oldTeamType = TeamType.valueOf(payload.get(3));
        PlayerType oldPlayerType = PlayerType.valueOf(payload.get(4));

        callback.clearOldLabel(oldTeamType, oldPlayerType);
        callback.setPlayerNameToLabel(clientName, teamType, playerType);
    }

    private void handleSessionStarted(SocketChannel clientChannel, List<String> payload) {
        PlayerType currentClientPlayerType = PlayerType.valueOf(payload.get(0));
        String currentTurnName = payload.get(1);
        int redCount = Integer.parseInt(payload.get(2));
        int blueCount = Integer.parseInt(payload.get(3));
        TeamType teamType = TeamType.valueOf(payload.get(4));
        PlayerType playerType = PlayerType.valueOf(payload.get(5));
        List<WordData> words = WordDataHelper.convertStringToWords(payload.get(6));

        callback.setCounters(redCount, blueCount);
        callback.generateWordCards(words, currentClientPlayerType);
        callback.setPlayerPermission(currentTurnName, teamType, playerType);
    }

    private void handleMessageAdded(SocketChannel clientChannel, List<String> payload) {
        String message = payload.get(0);
        TeamType teamType = TeamType.valueOf(payload.get(1));

        callback.addMessageToListView(message, teamType);
    }

    private void handleNextTurn(SocketChannel clientChannel, List<String> payload) {
        String currentTurnName = payload.get(0);
        TeamType teamType = TeamType.valueOf(payload.get(1));
        PlayerType playerType = PlayerType.valueOf(payload.get(2));
        callback.setPlayerPermission(currentTurnName, teamType, playerType);
    }

    private void handleCardClicked(SocketChannel clientChannel, List<String> payload) {
        int positionX = Integer.parseInt(payload.get(0));
        int positionY = Integer.parseInt(payload.get(1));
        int redCount = Integer.parseInt(payload.get(2));
        int blueCount = Integer.parseInt(payload.get(3));
        TeamType clientTeamType = TeamType.valueOf(payload.get(4));
        PlayerType playerType = PlayerType.valueOf(payload.get(5));
        TeamType wordTeamType = TeamType.valueOf(payload.get(6));
        boolean isTurnEnded = Boolean.parseBoolean(payload.get(7));
        TeamType clickedByTeamType = TeamType.valueOf(payload.get(8));
        PlayerType clickedByPlayerType = PlayerType.valueOf(payload.get(9));

        callback.setCounters(redCount, blueCount);
        callback.handleCardClicked(wordTeamType, playerType, positionX, positionY);
        if (isTurnEnded) {
            String rawMessage = String.format("END_TURN|%s|%s",
                    clickedByTeamType, clickedByPlayerType);
            networkService.sendMessage(clientChannel, rawMessage);
        }
    }
}
