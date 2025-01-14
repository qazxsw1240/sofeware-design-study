package com.example.chat.entity.websocket;

public class WebSocketUsernameResponse implements WebSocketMessage {

    private String sessionId;
    private String username;

    public WebSocketUsernameResponse() {
    }

    public WebSocketUsernameResponse(String sessionId, String username) {
        this.sessionId = sessionId;
        this.username = username;
    }

    @Override
    public String getSessionId() {
        return this.sessionId;
    }

    public String getUsername() {
        return this.username;
    }

}
