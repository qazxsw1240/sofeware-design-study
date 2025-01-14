package com.example.chat.service.websocket;

import com.example.chat.entity.User;
import com.example.chat.entity.UserAuth;
import com.example.chat.entity.UserAuthState;
import com.example.chat.event.websocket.WebSocketJsonMessageReceiveListener;
import com.example.chat.event.websocket.WebSocketSessionAddListener;
import com.example.chat.event.websocket.WebSocketSessionRemoveListener;
import com.example.chat.repository.SessionRepository;
import com.example.chat.repository.UserAuthRepository;
import com.example.chat.repository.UserRepository;
import com.example.chat.service.MessageBroker;
import com.example.chat.util.JsonNodeUtils;
import com.example.chat.websocket.WebSocketEventListenerManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class UserAuthService
        implements WebSocketSessionAddListener,
                   WebSocketSessionRemoveListener,
                   WebSocketJsonMessageReceiveListener,
                   DisposableBean {

    private static final Logger logger = LogManager.getLogger(UserAuthService.class);

    private final SessionRepository sessionRepository;
    private final UserAuthRepository userAuthRepository;
    private final UserRepository userRepository;
    private final WebSocketEventListenerManager webSocketEventListenerManager;

    private final MessageBroker messageBroker;

    public UserAuthService(
            SessionRepository sessionRepository,
            UserAuthRepository userAuthRepository,
            UserRepository userRepository,
            MessageBroker messageBroker,
            WebSocketEventListenerManager webSocketEventListenerManager) {
        this.sessionRepository = sessionRepository;
        this.userAuthRepository = userAuthRepository;
        this.userRepository = userRepository;
        this.webSocketEventListenerManager = webSocketEventListenerManager;
        this.messageBroker = messageBroker;
        this.webSocketEventListenerManager.addListener(this);
    }

    @Override
    public void onSessionAdd(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        this.sessionRepository.addEntity(session);
        this.userAuthRepository.addEntity(new UserAuth(sessionId, UserAuthState.IN_PROGRESS));
        JsonNode jsonData = JsonNodeFactory.instance.objectNode()
                .put("kind", "Auth#createUser")
                .put("sessionId", sessionId);
        this.messageBroker.sendMessage(sessionId, jsonData.toString());
    }

    @Override
    public void onSessionRemove(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        this.userRepository.removeEntityByKey(sessionId);
        this.userAuthRepository.removeEntityByKey(sessionId);
        this.sessionRepository.removeEntityByKey(sessionId);
    }

    @Override
    public void onJsonMessageReceive(WebSocketSession session, JsonNode jsonData) throws Exception {
        String sessionId = session.getId();
        if (this.userAuthRepository
                .findEntityByKey(sessionId)
                .filter(auth -> auth.getUserAuthState() == UserAuthState.SUCCESS)
                .isPresent()) {
            return;
        }
        if (!jsonData.get("kind").asText().equals("Auth#createUser")) {
            return;
        }
        String payloadSessionId = jsonData.get("sessionId").asText();
        if (!payloadSessionId.equals(sessionId)) {
            JsonNode errorMessageData = JsonNodeUtils.crateErrorMessageData(sessionId, "invalid session");
            this.messageBroker.sendMessage(sessionId, errorMessageData.toString());
            return;
        }
        this.userAuthRepository.updateUserAuth(new UserAuth(sessionId, UserAuthState.SUCCESS));
        createAuthenticatedUser(jsonData);
    }

    @Override
    public void destroy() {
        this.webSocketEventListenerManager.removeListener(this);
    }

    private void createAuthenticatedUser(JsonNode jsonData) {
        String sessionId = jsonData.get("sessionId").asText();
        String username = jsonData.get("username").asText();
        WebSocketSession session = this.sessionRepository
                .findEntityByKey(sessionId)
                .orElseThrow();
        User user = new User(session, username, LocalDateTime.now());
        this.userRepository.addEntity(user);
        logger.info("Authenticated User {} has joined.", username);
        JsonNode responseData = JsonNodeFactory.instance.objectNode()
                .put("kind", "Auth#authUser")
                .put("sessionId", user.getSessionId())
                .put("username", user.getName())
                .put("joinedTime", user
                        .getJoinedTime()
                        .format(DateTimeFormatter.ISO_DATE_TIME));
        this.messageBroker.sendMessage(sessionId, responseData.toString());
    }

}
