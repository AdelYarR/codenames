package ru.myapp.semester_work.utils;

import ru.myapp.semester_work.models.Message;
import ru.myapp.semester_work.models.MessageType;

import java.util.Arrays;
import java.util.List;

public class MessageParser {

    private MessageParser() {}

    public static Message parse(String rawMessage) {
        String[] parts = rawMessage.split("\\|");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Сообщение должно состоять минимум из двух элементов.");
        }

        MessageType type = MessageType.valueOf(parts[0]);
        List<String> payload = Arrays.stream(parts)
                .skip(1)
                .toList();

        return new Message(type, payload);
    }
}
