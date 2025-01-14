package com.example.chat.service;

import org.springframework.web.socket.WebSocketSession;

public interface MessageBroker {

    public void sendMessage(String sessionId, String message);

    public void sendMessage(String sessionId, Object message);

    public default void sendMessage(WebSocketSession session, String message) {
        sendMessage(session.getId(), message);
    }

    public default void sendMessage(WebSocketSession session, Object message) {
        sendMessage(session.getId(), message);
    }

}
