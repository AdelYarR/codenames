package ru.myapp.semester_work.server.service;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConnectionManager {

    private final ConcurrentHashMap<SocketChannel, String> clients;

    public ConnectionManager() {
        this.clients = new ConcurrentHashMap<>();
    }

    public void acceptNewConnection(ServerSocketChannel serverChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    public ConcurrentMap<SocketChannel, String> getClients() {
        return clients;
    }

    public String getClientUsername(SocketChannel clientChannel) {
        return clients.get(clientChannel);
    }

    public void addClient(SocketChannel clientChannel, String username) {
        clients.put(clientChannel, username);
    }

    public void removeClient(SocketChannel clientChannel) {
        clients.remove(clientChannel);
    }
}
