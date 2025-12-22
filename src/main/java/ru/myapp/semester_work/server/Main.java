package ru.myapp.semester_work.server;

import ru.myapp.semester_work.server.controller.ServerView;

public class Main {
    public static void main(String[] args) {
        ServerView serverView = new ServerView();
        serverView.start();
    }
}
