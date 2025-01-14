package com.example.chat.service.websocket;

import com.example.chat.entity.User;
import com.example.chat.entity.UserAuth;
import com.example.chat.entity.UserAuthState;
import com.example.chat.entity.websocket.WebSocketExceptionMessage;
import com.example.chat.entity.websocket.WebSocketUserRegisterResponse;
import com.example.chat.entity.websocket.WebSocketUsernameRequest;
import com.example.chat.entity.websocket.WebSocketUsernameResponse;
import com.example.chat.event.websocket.WebSocketSessionAddListener;
import com.example.chat.event.websocket.WebSocketSessionRemoveListener;
import com.example.chat.event.websocket.WebSocketTextMessageReceiveListener;
import com.example.chat.repository.SessionRepository;
import com.example.chat.repository.UserAuthRepository;
import com.example.chat.repository.UserRepository;
import com.example.chat.websocket.WebSocketEventListenerManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;

@Service
public class UserAuthService
        implements WebSocketSessionAddListener,
                   WebSocketSessionRemoveListener,
                   WebSocketTextMessageReceiveListener,
                   DisposableBean {

    private static final Logger logger = LogManager.getLogger(UserAuthService.class);

    private final SessionRepository sessionRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserRepository userRepository;
    private final WebSocketEventListenerManager webSocketEventListenerManager;

    private final SessionMessageBrokerService messageBrokerService;
    private final ObjectMapper objectMapper;

    public UserAuthService(
            SessionRepository sessionRepository,
            UserAuthRepository userAuthRepository,
            UserRepository userRepository,
            SessionMessageBrokerService messageBrokerService,
            ObjectMapper objectMapper,
            WebSocketEventListenerManager webSocketEventListenerManager) {
        this.sessionRepository = sessionRepository;
        this.userAuthRepository = userAuthRepository;
        this.userRepository = userRepository;
        this.webSocketEventListenerManager = webSocketEventListenerManager;
        this.messageBrokerService = messageBrokerService;
        this.objectMapper = objectMapper;
        this.webSocketEventListenerManager.addListener(this);
    }

    @Override
    public void onSessionAdd(WebSocketSession session) {
        String sessionId = session.getId();
        this.sessionRepository.addEntity(session);
        this.userAuthRepository.addEntity(new UserAuth(sessionId, UserAuthState.IN_PROGRESS));
        this.messageBrokerService.sendMessage(
                sessionId,
                new WebSocketUsernameRequest(sessionId, "require username"));
    }

    @Override
    public void onSessionRemove(WebSocketSession session, CloseStatus closeStatus) {
        String sessionId = session.getId();
        this.userRepository.removeEntityByKey(sessionId);
        this.userAuthRepository.removeEntityByKey(sessionId);
        this.sessionRepository.removeEntityByKey(sessionId);
    }

    @Override
    public void onTextMessageReceive(WebSocketSession session, TextMessage message) {
        String sessionId = session.getId();
        if (!this.sessionRepository.containsEntityByKey(sessionId)) {
            return;
        }
        String payload = message.getPayload();
        try {
            WebSocketUsernameResponse response = this.objectMapper.readValue(payload, WebSocketUsernameResponse.class);
            if (!response.getSessionId().equals(sessionId)) {
                this.messageBrokerService.sendMessage(
                        sessionId,
                        new WebSocketExceptionMessage(sessionId, "invalid session"));
                return;
            }
            this.userAuthRepository.updateUserAuth(new UserAuth(sessionId, UserAuthState.SUCCESS));
            createAuthenticatedUser(response);
        } catch (JsonProcessingException ignored) {
        }
    }

    @Override
    public void destroy() {
        this.webSocketEventListenerManager.removeListener(this);
    }

    private void createAuthenticatedUser(WebSocketUsernameResponse response) {
        String sessionId = response.getSessionId();
        String username = response.getUsername();
        WebSocketSession session = this.sessionRepository
                .findEntityByKey(sessionId)
                .orElseThrow();
        User user = new User(session, username, LocalDateTime.now());
        this.userRepository.addEntity(user);
        logger.info("Authenticated user {} has joined.", username);
        this.messageBrokerService.sendMessage(sessionId, new WebSocketUserRegisterResponse(user));
    }

}
