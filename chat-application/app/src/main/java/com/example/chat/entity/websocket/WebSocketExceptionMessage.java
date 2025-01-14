package com.example.chat.entity.websocket;

public class WebSocketExceptionMessage implements WebSocketMessage {

    private String sessionId;
    private String message;

    public WebSocketExceptionMessage() {
    }

    public WebSocketExceptionMessage(String sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
    }

    @Override
    public String getSessionId() {
        return this.sessionId;
    }

    public String getMessage() {
        return this.message;
    }

}
