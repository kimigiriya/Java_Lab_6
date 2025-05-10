package com.example.chat.client;

import com.example.shared.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private List<String> userList;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
        this.userList = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            while (socket.isConnected()) {
                Message message = (Message) in.readObject();
                handleMessage(message);
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Ошибка в обработчике клиента: {}", e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.error("Ошибка закрытия сокета: {}", e.getMessage());
            }
        }
    }

    private void handleMessage(Message message) {
        switch (message.getType()) {
            case BROADCAST:
                System.out.printf("\n[Публичное от %s]: %s\n",
                        message.getSender(), message.getContent());
                break;
            case PRIVATE:
                System.out.printf("\n[Приватное от %s]: %s\n",
                        message.getSender(), message.getContent());
                break;
            case USER_LIST:
                synchronized (this) {
                    userList = List.of(message.getContent().split(","));
                    notifyAll();
                }
                break;
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            logger.error("Ошибка отправки сообщения: {}", e.getMessage());
        }
    }

    public synchronized List<String> getUserList() {
        try {
            wait(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new ArrayList<>(userList);
    }
}