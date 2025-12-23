package ru.myapp.semester_work.client.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class NetworkService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkService.class);
    private static final String MESSAGE_DELIMITER = "\n";

    public void sendMessage(SocketChannel clientChannel, String message) {
        if (clientChannel == null || !clientChannel.isOpen()) {
            LOGGER.error("Не удалось отправить сообщение. Сервер закрыт или не существует.");
            return;
        }

        try {
            String messageWithDelimiter = message + MESSAGE_DELIMITER;
            byte[] bytes = messageWithDelimiter.getBytes(StandardCharsets.UTF_8);
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            clientChannel.write(buffer);

            LOGGER.info("Отправлено сообщение серверу: {}", message);
        } catch (IOException e) {
            LOGGER.error("Не удалось отправить сообщение серверу.", e);
        }
    }
}
