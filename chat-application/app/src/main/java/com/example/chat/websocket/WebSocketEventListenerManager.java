package com.example.chat.websocket;

import com.example.chat.event.EventListenerManager;
import com.example.chat.event.websocket.WebSocketEventListener;

public interface WebSocketEventListenerManager
        extends EventListenerManager<WebSocketEventListener> {
}
