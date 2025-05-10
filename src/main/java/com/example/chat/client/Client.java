package com.example.chat.client;

import com.example.shared.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите своё имя: ");
        String nickname = scanner.nextLine();

        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Подключён к серверу по " + SERVER_ADDRESS + ":" + SERVER_PORT);

            // Send nickname to server
            Message connectMessage = new Message(nickname, null, null, Message.MessageType.CONNECT);
            ClientHandler clientHandler = new ClientHandler(socket);
            clientHandler.sendMessage(connectMessage);

            // Start message receiver in separate thread
            new Thread(clientHandler).start();

            // Main menu loop
            while (true) {
                printMenu();
                int choice = getIntInput(scanner, 1, 3);

                if (choice == 3) {
                    sendDisconnectMessage(clientHandler, nickname);
                    break;
                }

                System.out.print("Введите своё сообщение: ");
                String content = scanner.nextLine();

                if (choice == 1) {
                    sendBroadcastMessage(clientHandler, nickname, content);
                } else if (choice == 2) {
                    sendPrivateMessage(clientHandler, nickname, content, scanner);
                }
            }
        } catch (IOException e) {
            logger.error("Ошибка клиента: {}", e.getMessage());
            System.err.println("Ошибка подключения к серверу: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void printMenu() {
        System.out.println("\nВыберите действие:");
        System.out.println("1. Публичное сообщение");
        System.out.println("2. Приватное сообщение");
        System.out.println("3. Выход");
        System.out.print("Ваш выбор: ");
    }

    private static int getIntInput(Scanner scanner, int min, int max) {
        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice >= min && choice <= max) {
                    return choice;
                }
                System.out.print("Введите число между " + min + " и " + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("Неверный ввод. Введите число: ");
            }
        }
    }

    private static void sendBroadcastMessage(ClientHandler clientHandler, String nickname, String content) {
        Message message = new Message(
                nickname,
                null,
                content,
                Message.MessageType.BROADCAST
        );
        clientHandler.sendMessage(message);
    }

    private static void sendPrivateMessage(ClientHandler clientHandler, String nickname,
                                           String content, Scanner scanner) {
        System.out.println("Запрос списка пользователей...");
        Message userListRequest = new Message(
                nickname,
                null,
                null,
                Message.MessageType.USER_LIST
        );
        clientHandler.sendMessage(userListRequest);

        List<String> users = clientHandler.getUserList();
        if (users == null || users.isEmpty()) {
            System.out.println("Нет других подключенных пользователей.");
            return;
        }

        System.out.println("Подключенные  пользователи:");
        for (int i = 0; i < users.size(); i++) {
            System.out.println((i + 1) + ". " + users.get(i));
        }

        System.out.print("Выберите получателя(номер): ");
        int recipientIndex = getIntInput(scanner, 1, users.size()) - 1;

        String recipient = users.get(recipientIndex);
        Message message = new Message(
                nickname,
                recipient,
                content,
                Message.MessageType.PRIVATE
        );
        clientHandler.sendMessage(message);
    }

    private static void sendDisconnectMessage(ClientHandler clientHandler, String nickname) {
        Message disconnectMessage = new Message(
                nickname,
                null,
                null,
                Message.MessageType.DISCONNECT
        );
        clientHandler.sendMessage(disconnectMessage);
        System.out.println("Отключение от сервера. Сайонара!");
    }
}