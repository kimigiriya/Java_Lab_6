package com.example.shared;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sender;
    private String recipient;
    private String content;
    private MessageType type;

    public Message(String sender, String recipient, String content, MessageType type) {
        this.sender = sender;
        this.recipient = recipient;
        this.content = content;
        this.type = type;
    }

    public enum MessageType {
        BROADCAST, PRIVATE, CONNECT, DISCONNECT, USER_LIST
    }

    // Getters
    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("Сообщение{Отправитель='%s', Получатель='%s', Содержание='%s', Тип=%s}",
                sender, recipient, content, type);
    }
}