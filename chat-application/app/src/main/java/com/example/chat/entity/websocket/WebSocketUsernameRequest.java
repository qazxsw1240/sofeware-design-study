package com.example.chat.entity.websocket;

@Deprecated
public class WebSocketUsernameRequest extends AbstractWebSocketRequest {
    
    private String status;

    public WebSocketUsernameRequest() {
    }

    public WebSocketUsernameRequest(
            String sessionId,
            String status) {
        super("user_request", sessionId);
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

}
