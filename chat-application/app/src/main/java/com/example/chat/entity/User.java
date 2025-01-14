package com.example.chat.entity;

import org.springframework.web.socket.WebSocketSession;

import java.io.Serializable;
import java.time.LocalDateTime;

public class User implements Entity, Serializable {

    private final WebSocketSession session;
    private final String name;
    private final LocalDateTime joinedTime;

    public User(WebSocketSession session, String name, LocalDateTime joinedTime) {
        this.session = session;
        this.name = name;
        this.joinedTime = joinedTime;
    }

    public String getSessionId() {
        return this.session.getId();
    }

    public WebSocketSession getSession() {
        return this.session;
    }

    public String getName() {
        return this.name;
    }

    public LocalDateTime getJoinedTime() {
        return this.joinedTime;
    }

    @Override
    public String toString() {
        return String.format("User[sessionId='%s', name='%s', joinedTime=%s]",
                this.session.getId(),
                this.name,
                this.joinedTime);
    }

}
