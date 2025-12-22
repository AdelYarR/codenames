package ru.myapp.semester_work.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerService.class);

    private final int port;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private final NetworkService networkService;
    private final ConnectionManager connectionManager;
    private final RoomManager roomManager;
    private final SessionManager sessionManager;
    private final MessageHandler messageHandler;
    private boolean isRunning;

    public ServerService(int port) {
        this.port = port;
        this.networkService = new NetworkService();
        this.connectionManager = new ConnectionManager();
        this.roomManager = new RoomManager();
        this.sessionManager = new SessionManager();
        this.messageHandler = new MessageHandler(networkService, connectionManager, sessionManager, roomManager);
    }

    public void start() {
        try {
            init();
            isRunning = true;
            LOGGER.info("Сервер запущен на порту {}", port);
            runLoop();
        } catch (IOException e) {
            LOGGER.error("Не удалось запустить сервер на порту {}", port, e);
            isRunning = false;
        }
    }

    public void init() throws IOException {
        serverChannel = ServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress(port));
        serverChannel.configureBlocking(false);

        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void runLoop() throws IOException {
        while (isRunning) {
            selector.select(this::handleSelection);
        }
    }

    private void handleSelection(SelectionKey key) {
        if (key.isAcceptable()) {
            handleAccept(key);
        }
        if (key.isReadable()) {
            handleRead(key);
        }
    }

    private void handleAccept(SelectionKey key) {
        try {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
            connectionManager.acceptNewConnection(serverSocketChannel, selector);
        } catch (IOException e) {
            LOGGER.error("Не удалось принять новое подключение", e);
        }
    }

    private void handleRead(SelectionKey key) {
        try {
            SocketChannel clientChannel = (SocketChannel) key.channel();
            clientChannel.configureBlocking(false);
            messageHandler.handleClientMessage(clientChannel);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Не удалось спарсить сообщение клиента", e);
        } catch (IOException e) {
            LOGGER.error("Не удалось прочитать сообщение клиента", e);
        }
    }
}
