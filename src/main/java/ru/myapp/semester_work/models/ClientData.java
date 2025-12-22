package ru.myapp.semester_work.models;

import java.net.InetAddress;

public record ClientData(
        String name,
        InetAddress address,
        int port
) {
}
