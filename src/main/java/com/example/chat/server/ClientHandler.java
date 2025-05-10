package com.example.chat.server;

import com.example.shared.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String userNickname;
    private List<ClientHandler> clients;

    public ClientHandler(Socket socket, List<ClientHandler> clients) throws IOException {
        this.socket = socket;
        this.clients = clients;
        this.out = new ObjectOutputStream(socket.getOutputStream());
        this.in = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        try {
            // First message should be the nickname
            Message connectMessage = (Message) in.readObject();
            if (connectMessage.getType() == Message.MessageType.CONNECT) {
                this.userNickname = connectMessage.getSender();
                logger.info("Пользователь '{}' подключён!", userNickname);

                // Notify all clients about new connection
                Message broadcastMessage = new Message(
                        userNickname,
                        null,
                        userNickname + " присоеденился к чату!",
                        Message.MessageType.BROADCAST
                );
                Server.broadcastMessage(broadcastMessage, this);
            }

            while (socket.isConnected()) {
                Message message = (Message) in.readObject();

                switch (message.getType()) {
                    case DISCONNECT:
                        handleDisconnect();
                        return;
                    case PRIVATE:
                        handlePrivateMessage(message);
                        break;
                    case BROADCAST:
                        handleBroadcastMessage(message);
                        break;
                    case USER_LIST:
                        sendUserList();
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Ошибка в клиентском обработчике для пользователя {}: {}", userNickname, e.getMessage());
            handleDisconnect();
        }
    }

    private void handlePrivateMessage(Message message) {
        logger.info("Личное сообщения от {} для {}: {}",
                message.getSender(), message.getRecipient(), message.getContent());
        Server.sendPrivateMessage(message);
    }

    private void handleBroadcastMessage(Message message) {
        logger.info("публичное сообщение от {}: {}",
                message.getSender(), message.getContent());
        Server.broadcastMessage(message, this);
    }

    private void handleDisconnect() {
        try {
            if (userNickname != null) {
                logger.info("Пользователь '{}' отключился", userNickname);
                Message disconnectMessage = new Message(
                        userNickname,
                        null,
                        userNickname + " покинул чат(",
                        Message.MessageType.BROADCAST
                );
                Server.broadcastMessage(disconnectMessage, this);
            }
            clients.remove(this);
            socket.close();
        } catch (IOException e) {
            logger.error("Ошибка при закрытии подключения(сеанса) для {}: {}", userNickname, e.getMessage());
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            logger.error("Ошибка отправки сообщения {}: {}", userNickname, e.getMessage());
        }
    }

    private void sendUserList() {
        List<String> userList = Server.getConnectedUsers();
        Message userListMessage = new Message(
                "SERVER",
                userNickname,
                String.join(",", userList),
                Message.MessageType.USER_LIST
        );
        sendMessage(userListMessage);
    }

    public String getUserNickname() {
        return userNickname;
    }
}