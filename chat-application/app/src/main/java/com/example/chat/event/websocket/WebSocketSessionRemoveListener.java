package com.example.chat.event.websocket;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

public interface WebSocketSessionRemoveListener extends WebSocketEventListener {

    public void onSessionRemove(WebSocketSession session, CloseStatus closeStatus);

}
