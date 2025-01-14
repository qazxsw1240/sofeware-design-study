package com.example.chat.websocket;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.example.chat.event.EventListenerManagerBase;
import com.example.chat.event.websocket.WebSocketEventListener;
import com.example.chat.event.websocket.WebSocketSessionAddListener;
import com.example.chat.event.websocket.WebSocketSessionRemoveListener;
import com.example.chat.event.websocket.WebSocketTextMessageReceiveListener;

public class WebSocketHandlerImpl
        extends TextWebSocketHandler
        implements WebSocketEventListenerManager, DisposableBean {

    private static final Logger logger = LogManager.getLogger(WebSocketHandlerImpl.class);

    private final WebSocketEventListenerManagerImpl listenerManager;

    public WebSocketHandlerImpl() {
        this.listenerManager = new WebSocketEventListenerManagerImpl();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String id = session.getId();
        logger.info("New session created: {}", id);
        this.listenerManager
                .getWebSocketSessionAddListeners()
                .forEach(listener -> listener.onSessionAdd(session));
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String id = session.getId();
        logger.info("Session {} sent a text message: {}", id, message.getPayload());
        this.listenerManager
                .getWebSocketTextMessageReceiveListeners()
                .forEach(listener -> listener.onTextMessageReceive(session, message));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String id = session.getId();
        logger.info("Session closed: {}", id);
        this.listenerManager
                .getWebSocketSessionRemoveListeners()
                .forEach(listener -> listener.onSessionRemove(session, closeStatus));
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public void addListener(WebSocketEventListener listener) {
        this.listenerManager.addListener(listener);
    }

    @Override
    public void removeListener(WebSocketEventListener listener) {
        this.listenerManager.removeListener(listener);
    }

    @Override
    public void destroy() throws Exception {
        this.listenerManager.removeAllListeners();
    }

    private static class WebSocketEventListenerManagerImpl
            extends EventListenerManagerBase<WebSocketEventListener> {

        public WebSocketEventListenerManagerImpl() {
        }

        public List<WebSocketSessionAddListener> getWebSocketSessionAddListeners() {
            return getListeners(WebSocketSessionAddListener.class);
        }

        public List<WebSocketSessionRemoveListener> getWebSocketSessionRemoveListeners() {
            return getListeners(WebSocketSessionRemoveListener.class);
        }

        public List<WebSocketTextMessageReceiveListener> getWebSocketTextMessageReceiveListeners() {
            return getListeners(WebSocketTextMessageReceiveListener.class);
        }

        public void removeAllListeners() {
            this.listeners.clear();
        }

    }

}
