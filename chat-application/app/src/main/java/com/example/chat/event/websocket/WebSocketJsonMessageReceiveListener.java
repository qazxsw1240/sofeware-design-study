package com.example.chat.event.websocket;

import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.databind.JsonNode;

public interface WebSocketJsonMessageReceiveListener extends WebSocketEventListener {

    public void onJsonMessageReceive(WebSocketSession session, JsonNode jsonData) throws Exception;

}
