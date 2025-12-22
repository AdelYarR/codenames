package ru.myapp.semester_work.server.controller;

import ru.myapp.semester_work.server.service.ServerService;

import java.util.InputMismatchException;
import java.util.Scanner;

public class ServerView {

    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            try {
                System.out.print("Введите номер порта: ");
                int port = scanner.nextInt();
                scanner.nextLine();

                ServerService serverService = new ServerService(port);
                serverService.start();
                break;
            } catch (InputMismatchException _) {
                System.out.println("Получено недопустимое значение. Пожалуйста, введите число от 0 до 65535.");
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("На стороне сервера произошла ошибка: " + e);
                scanner.nextLine();
            }
        }
    }
}
