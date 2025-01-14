package com.example.chat.event.websocket;

import org.springframework.web.socket.WebSocketSession;

public interface WebSocketSessionAddListener extends WebSocketEventListener {

    public void onSessionAdd(WebSocketSession session);

}
