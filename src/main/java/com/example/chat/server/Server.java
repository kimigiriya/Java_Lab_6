package com.example.chat.server;

import com.example.shared.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int PORT = 12345;
    private static final List<ClientHandler> clients = new ArrayList<>();
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Порт чат-приложения: {}", PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Новый пользователь подключён: {}", clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket, clients);
                clients.add(clientHandler);
                pool.execute(clientHandler);
            }
        } catch (IOException e) {
            logger.error("Ошибка сервера: {}", e.getMessage(), e);
        } finally {
            pool.shutdown();
        }
    }

    public static void broadcastMessage(Message message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender && client.getUserNickname() != null) {
                client.sendMessage(message);
            }
        }
    }

    public static void sendPrivateMessage(Message message) {
        for (ClientHandler client : clients) {
            if (client.getUserNickname() != null &&
                    client.getUserNickname().equals(message.getRecipient())) {
                client.sendMessage(message);
                break;
            }
        }
    }

    public static List<String> getConnectedUsers() {
        List<String> userList = new ArrayList<>();
        for (ClientHandler client : clients) {
            if (client.getUserNickname() != null) {
                userList.add(client.getUserNickname());
            }
        }
        return userList;
    }
}