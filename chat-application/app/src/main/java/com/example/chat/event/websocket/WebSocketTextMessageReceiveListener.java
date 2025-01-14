package com.example.chat.event.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public interface WebSocketTextMessageReceiveListener extends WebSocketEventListener {

    public void onTextMessageReceive(WebSocketSession session, TextMessage message);

}
