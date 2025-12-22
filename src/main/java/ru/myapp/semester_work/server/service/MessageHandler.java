package ru.myapp.semester_work.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.myapp.semester_work.models.*;
import ru.myapp.semester_work.utils.MessageParser;
import ru.myapp.semester_work.utils.WordDataHelper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);
    private static final int BUFFER_SIZE = 1024;

    private final NetworkService networkService;
    private final ConnectionManager connectionManager;
    private final SessionManager sessionManager;
    private final RoomManager roomManager;

    public MessageHandler(NetworkService networkService, ConnectionManager connectionManager,
                          SessionManager sessionManager, RoomManager roomManager) {
        this.networkService = networkService;
        this.connectionManager = connectionManager;
        this.sessionManager = sessionManager;
        this.roomManager = roomManager;
    }

    public void handleClientMessage(SocketChannel clientChannel) {
        String rawMessage = readMessage(clientChannel);
        if (rawMessage == null || rawMessage.isEmpty()) {
            return;
        }

        LOGGER.info("Получено сообщение от {}: {}", getClientInfo(clientChannel), rawMessage);

        Message message = MessageParser.parse(rawMessage);
        processMessage(clientChannel, message);
    }

    private String readMessage(SocketChannel clientChannel) {
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        try {
            int read = clientChannel.read(buffer);
            buffer.flip();
            return switch (read) {
                case -1 -> throw new IOException("Клиент отключился");
                case 0 -> "";
                default -> StandardCharsets.UTF_8.decode(buffer).toString().trim();
            };
        } catch (IOException e) {
            handleClientDisconnection(clientChannel, e);
            return null;
        }
    }

    private void processMessage(SocketChannel clientChannel, Message message) {
        switch (message.getType()) {
            case SET_CLIENT:
                handleSetClient(clientChannel, message.getPayload());
                break;
            case GET_ROOMS:
                handleGetRooms(clientChannel);
                break;
            case CREATE_ROOM:
                handleCreateRoom(message.getPayload());
                break;
            case JOIN_ROOM:
                handleJoinRoom(clientChannel, message.getPayload());
                break;
            case JOIN_TEAM:
                handleJoinTeam(clientChannel, message.getPayload());
                break;
            case START_SESSION:
                handleStartSession(clientChannel, message.getPayload());
                break;
            case END_TURN:
                handleEndTurn(clientChannel, message.getPayload());
                break;
            case ADD_MESSAGE:
                handleAddMessage(clientChannel, message.getPayload());
                break;
            case CLICK_CARD:
                handleClickCard(clientChannel, message.getPayload());
                break;
            default:
                handleError(clientChannel);
                break;
        }
    }

    private void handleClientDisconnection(SocketChannel clientChannel, IOException e) {
        String clientUsername = connectionManager.getClientUsername(clientChannel);
        if (clientUsername == null) {
            LOGGER.error("Клиент уже отключен", e);
            return;
        }

        connectionManager.removeClient(clientChannel);
    }

    private void handleError(SocketChannel clientChannel) {
        LOGGER.error("Неподходящий тип сообщения для сервера");
    }

    private String getClientInfo(SocketChannel clientChannel) {
        String clientUsername = connectionManager.getClientUsername(clientChannel);
        if (clientUsername != null) {
            return clientUsername;
        }
        try {
            return clientChannel.getRemoteAddress().toString();
        } catch (IOException e) {
            LOGGER.error("Не удалось получить информацию о клиенте", e);
            return null;
        }
    }

    private void handleSetClient(SocketChannel clientChannel, List<String> payload) {
        String clientUsername = payload.getFirst();
        connectionManager.addClient(clientChannel, clientUsername);
        LOGGER.info("Клиент {} успешно подключился и добавлен в список сервера", clientUsername);
        String rawMessage = "CLIENT_ADDED|" + clientUsername;
        networkService.sendMessage(clientChannel, getClientInfo(clientChannel), rawMessage);
    }

    private void handleGetRooms(SocketChannel clientChannel) {
        List<String> roomNames = roomManager.getRoomNames();
        String roomsString = String.join("|", roomNames);
        String rawMessage = "ROOMS_GOT|" + roomsString;
        networkService.sendMessage(clientChannel, getClientInfo(clientChannel), rawMessage);
    }

    private void handleCreateRoom(List<String> payload) {
        String roomName = payload.getFirst();
        roomManager.addRoom(roomName);
        broadcastRoomsUpdate();
    }

    private void handleJoinRoom(SocketChannel clientChannel, List<String> payload) {
        String roomName = payload.getFirst();
        try {
            roomManager.addClientToRoom(clientChannel, roomName);
            String rawMessage = "ROOM_JOINED|" + roomName;
            networkService.sendMessage(clientChannel, getClientInfo(clientChannel), rawMessage);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Не удалось добавить клиента {} в комнату {}", getClientInfo(clientChannel), roomName, e);
        }
    }

    private void broadcastRoomsUpdate() {
        List<String> roomNames = roomManager.getRoomNames();
        String roomsList = String.join("|", roomNames);
        String rawMessage = "ROOMS_GOT|" + roomsList;
        for (SocketChannel clientChannel : connectionManager.getClients().keySet()) {
            networkService.sendMessage(clientChannel, getClientInfo(clientChannel), rawMessage);
        }
    }

    private void handleJoinTeam(SocketChannel clientChannel, List<String> payload) {
        String clientName = payload.get(0);
        TeamType teamType = TeamType.valueOf(payload.get(1));
        PlayerType playerType = PlayerType.valueOf(payload.get(2));

        Optional<String> optionalRoomName = roomManager.getRoomNameByClientChannel(clientChannel);
        if (optionalRoomName.isEmpty()) {
            LOGGER.error("Клиент {} не находится ни в какой комнате", clientName);
            return;
        }
        String roomName = optionalRoomName.get();

        Optional<SessionState> optionalSessionState = sessionManager.getSessionStateByRoomName(roomName);
        SessionState sessionState;
        if (optionalSessionState.isEmpty()) {
            sessionState = sessionManager.addAndGetSessionState(roomName);
            LOGGER.info("Создана новая сессия для комнаты {}", roomName);
        } else {
            sessionState = optionalSessionState.get();
        }

        TeamType oldTeamType = sessionState.getTeamTypeByClientChannel(clientChannel);
        PlayerType oldPlayerType = sessionState.getPlayerTypeByClientChannel(clientChannel);

        sessionManager.updateClientType(roomName, clientChannel, teamType, playerType, oldTeamType, oldPlayerType);

        String rawMessage = String.format("TEAM_JOINED|%s|%s|%s|%s|%s", clientName, teamType, playerType, oldTeamType, oldPlayerType);
        broadcastToRoom(roomManager.getClientChannelsByRoomName(roomName), rawMessage);
    }

    private void broadcastToRoom(List<SocketChannel> clientChannels, String rawMessage) {
        for (SocketChannel clientChannel : clientChannels) {
            networkService.sendMessage(clientChannel, getClientInfo(clientChannel), rawMessage);
        }
    }

    private void handleStartSession(SocketChannel clientChannel, List<String> payload) {
        String clientName = payload.getFirst();

        Optional<String> optionalRoomName = roomManager.getRoomNameByClientChannel(clientChannel);
        if (optionalRoomName.isEmpty()) {
            LOGGER.error("Клиент {} не находится ни в какой комнате", clientName);
            return;
        }
        String roomName = optionalRoomName.get();

        Optional<SessionState> optionalSessionState = sessionManager.getSessionStateByRoomName(roomName);
        if (optionalSessionState.isEmpty()) {
            LOGGER.info("Сессия для комнаты {} не создана", roomName);
            return;
        }
        SessionState sessionState = optionalSessionState.get();

        sessionState.start();

        broadcastStartToRoom(roomManager.getClientChannelsByRoomName(roomName), sessionState);
    }

    private void broadcastStartToRoom(List<SocketChannel> clientChannels, SessionState sessionState) {
        SocketChannel currentTurn = sessionState.getCurrentTurn();
        List<WordData> words = sessionState.getWords();

        for (SocketChannel clientChannel : clientChannels) {
            String rawMessage = String.format("SESSION_STARTED|%s|%s|%s|%s|%s|%s|%s",
                    sessionState.getPlayerTypeByClientChannel(clientChannel),
                    getClientInfo(currentTurn),
                    sessionState.getRedCount(),
                    sessionState.getBlueCount(),
                    sessionState.getTeamTypeByClientChannel(currentTurn),
                    sessionState.getPlayerTypeByClientChannel(currentTurn),
                    WordDataHelper.convertWordsToString(words));
            networkService.sendMessage(clientChannel, getClientInfo(clientChannel), rawMessage);
        }
    }

    private void handleAddMessage(SocketChannel clientChannel, List<String> payload) {
        Optional<String> optionalRoomName = roomManager.getRoomNameByClientChannel(clientChannel);
        if (optionalRoomName.isEmpty()) {
            LOGGER.error("Клиент не находится ни в какой комнате");
            return;
        }
        String roomName = optionalRoomName.get();

        String rawMessage = String.format("MESSAGE_ADDED|%s|%s", payload.get(0), payload.get(1));
        broadcastToRoom(roomManager.getClientChannelsByRoomName(roomName), rawMessage);
    }

    private void handleEndTurn(SocketChannel clientChannel, List<String> payload) {
        TeamType teamType = TeamType.valueOf(payload.get(0));
        PlayerType playerType = PlayerType.valueOf(payload.get(1));

        Optional<String> optionalRoomName = roomManager.getRoomNameByClientChannel(clientChannel);
        if (optionalRoomName.isEmpty()) {
            LOGGER.error("Клиент не находится ни в какой комнате");
            return;
        }
        String roomName = optionalRoomName.get();

        Optional<SessionState> optionalSessionState = sessionManager.getSessionStateByRoomName(roomName);
        if (optionalSessionState.isEmpty()) {
            LOGGER.info("Сессия для комнаты {} не создана", roomName);
            return;
        }
        SessionState sessionState = optionalSessionState.get();

        sessionState.switchTurn(teamType, playerType);

        SocketChannel currentTurn = sessionState.getCurrentTurn();
        String rawMessage = String.format("NEXT_TURN|%s|%s|%s",
                getClientInfo(currentTurn),
                sessionState.getTeamTypeByClientChannel(currentTurn),
                sessionState.getPlayerTypeByClientChannel(currentTurn));
        broadcastToRoom(roomManager.getClientChannelsByRoomName(roomName), rawMessage);
    }

    private void handleClickCard(SocketChannel clientChannel, List<String> payload) {
        int positionX = Integer.parseInt(payload.get(0));
        int positionY = Integer.parseInt(payload.get(1));

        Optional<String> optionalRoomName = roomManager.getRoomNameByClientChannel(clientChannel);
        if (optionalRoomName.isEmpty()) {
            LOGGER.error("Клиент не находится ни в какой комнате");
            return;
        }
        String roomName = optionalRoomName.get();

        Optional<SessionState> optionalSessionState = sessionManager.getSessionStateByRoomName(roomName);
        if (optionalSessionState.isEmpty()) {
            LOGGER.info("Сессия для комнаты {} не создана", roomName);
            return;
        }
        SessionState sessionState = optionalSessionState.get();

        int index = positionX * 5 + positionY;
        WordData wordData = sessionState.getWordDataByIndex(index);
        wordData.setGuessed(true);

        TeamType teamType = sessionState.getTeamTypeByClientChannel(clientChannel);

        String rawMessage;
        if (wordData.getTeamType() == teamType) {
            rawMessage = "CARD_CLICKED_SUCCESS";
            if (teamType == TeamType.RED) {
                sessionState.setRedCount(sessionState.getRedCount() - 1);
            } else {
                sessionState.setBlueCount(sessionState.getBlueCount() - 1);
            }
        } else {
            if (teamType == TeamType.NONE) {
                rawMessage = "CARD_CLICKED_NEUTRAL";
            } else {
                rawMessage = "CARD_CLICKED_UNSUCCESS";
                if (teamType == TeamType.RED) {
                    sessionState.setBlueCount(sessionState.getBlueCount() - 1);
                } else {
                    sessionState.setRedCount(sessionState.getRedCount() - 1);
                }
            }
        }

        rawMessage += String.format("|%s|%s|%s|%s");
        broadcastToRoom(roomManager.getClientChannelsByRoomName(roomName), rawMessage);
    }
}
