package com.example.chat.entity.websocket;

public class AbstractWebSocketRequest implements WebSocketRequest {

    protected String type;
    protected String sessionId;

    protected AbstractWebSocketRequest() {
    }

    protected AbstractWebSocketRequest(String type, String sessionId) {
        this.type = type;
        this.sessionId = sessionId;
    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public String getSessionId() {
        return this.sessionId;
    }

}
