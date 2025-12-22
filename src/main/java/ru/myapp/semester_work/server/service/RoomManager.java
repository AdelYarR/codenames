package ru.myapp.semester_work.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {

    private final ConcurrentHashMap<String, List<SocketChannel>> rooms;
    private final ConcurrentHashMap<SocketChannel, String> clientToRoomMap;

    public RoomManager() {
        this.rooms = new ConcurrentHashMap<>();
        this.clientToRoomMap = new ConcurrentHashMap<>();
        rooms.put("Комната 42-братух", new ArrayList<>());
        rooms.put("Team Spirit vs Team Falcons", new ArrayList<>());
    }

    public List<String> getRoomNames() {
        return new ArrayList<>(rooms.keySet());
    }

    public Optional<String> getRoomNameByClientChannel(SocketChannel clientChannel) {
        return Optional.of(clientToRoomMap.get(clientChannel));
    }

    public List<SocketChannel> getClientChannelsByRoomName(String roomName) {
        return rooms.get(roomName);
    }

    public void addRoom(String roomName) {
        rooms.put(roomName, new ArrayList<>());
    }

    public void addClientToRoom(SocketChannel clientChannel, String roomName) {
        if (roomName.isEmpty() || !rooms.containsKey(roomName)) {
            throw new IllegalArgumentException("Комната " + roomName + " не создана.");
        }

        List<SocketChannel> clients = rooms.get(roomName);
        if (clients.contains(clientChannel)) {
            throw new IllegalArgumentException("Клиент уже находится в комнате.");
        }

        if (clientToRoomMap.containsKey(clientChannel)) {
            throw new IllegalArgumentException("Клиент уже находится в комнате.");
        }

        clients.add(clientChannel);
        clientToRoomMap.put(clientChannel, roomName);
    }
}
