package com.example.chat.configuration;

import com.example.chat.websocket.WebSocketEventListenerManager;
import com.example.chat.websocket.WebSocketHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfigurererImpl implements WebSocketConfigurer {

    private static final int MAX_BUFFER_SIZE = 8192;

    private final WebSocketHandlerImpl webSocketHandler;

    public WebSocketConfigurererImpl() {
        this.webSocketHandler = new WebSocketHandlerImpl();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(getWebSocketHandler(), "/chat")
                .setAllowedOrigins("*");
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(MAX_BUFFER_SIZE);
        container.setMaxBinaryMessageBufferSize(MAX_BUFFER_SIZE);
        return container;
    }

    @Bean
    WebSocketHandler getWebSocketHandler() {
        return this.webSocketHandler;
    }

    @Bean
    WebSocketEventListenerManager getWebSocketEventListenerManager() {
        return this.webSocketHandler;
    }

}
