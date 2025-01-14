package com.example.chat.event.websocket;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.socket.WebSocketSession;

public interface WebSocketJsonMessageReceiveListener extends WebSocketEventListener {

    public void onJsonMessageReceive(WebSocketSession session, JsonNode jsonData) throws Exception;

}
