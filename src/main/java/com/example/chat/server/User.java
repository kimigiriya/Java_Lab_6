package com.example.chat.server;

public class User {
    private final String nickname;

    public User(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    @Override
    public String toString() {
        return nickname;
    }
}