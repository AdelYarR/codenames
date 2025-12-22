package ru.myapp.semester_work.models;

import java.util.List;

public class Message {

    private final MessageType type;
    private final List<String> payload;

    public Message(MessageType type, List<String> payload) {
        this.type = type;
        this.payload = payload;
    }

    public MessageType getType() {
        return type;
    }

    public List<String> getPayload() {
        return payload;
    }
}
