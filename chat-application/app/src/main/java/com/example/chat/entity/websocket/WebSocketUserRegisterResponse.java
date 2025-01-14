package com.example.chat.entity.websocket;

import com.example.chat.entity.User;

import java.time.LocalDateTime;

public class WebSocketUserRegisterResponse implements WebSocketMessage {

    private String sessionId;
    private String username;
    private LocalDateTime joinedTime;

    public WebSocketUserRegisterResponse() {
    }

    public WebSocketUserRegisterResponse(User user) {
        this(user.getSessionId(), user.getName(), user.getJoinedTime());
    }

    public WebSocketUserRegisterResponse(String sessionId, String username, LocalDateTime joinedTime) {
        this.sessionId = sessionId;
        this.username = username;
        this.joinedTime = joinedTime;
    }

    @Override
    public String getSessionId() {
        return this.sessionId;
    }

    public String getUsername() {
        return this.username;
    }

    public LocalDateTime getJoinedTime() {
        return this.joinedTime;
    }

}
