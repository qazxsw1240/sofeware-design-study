package com.example.chat.service.websocket;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.example.chat.entity.Room;
import com.example.chat.entity.User;
import com.example.chat.entity.UserAuthState;
import com.example.chat.event.websocket.WebSocketJsonMessageReceiveListener;
import com.example.chat.repository.RoomRepository;
import com.example.chat.repository.UserAuthRepository;
import com.example.chat.repository.UserRepository;
import com.example.chat.service.MessageBroker;
import com.example.chat.util.JsonNodeUtils;
import com.example.chat.websocket.WebSocketEventListenerManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class RoomLifecycleService implements WebSocketJsonMessageReceiveListener, DisposableBean {

    private static final Logger logger = LogManager.getLogger(RoomLifecycleService.class);

    private final UserAuthRepository userAuthRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final WebSocketEventListenerManager webSocketEventListenerManager;
    private final MessageBroker messageBroker;

    public RoomLifecycleService(
            UserAuthRepository userAuthRepository,
            UserRepository userRepository,
            RoomRepository roomRepository,
            WebSocketEventListenerManager webSocketEventListenerManager,
            MessageBroker messageBroker) {
        this.userAuthRepository = userAuthRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.webSocketEventListenerManager = webSocketEventListenerManager;
        this.messageBroker = messageBroker;
        this.webSocketEventListenerManager.addListener(this);
    }

    @Override
    public void onJsonMessageReceive(WebSocketSession session, JsonNode jsonData) throws Exception {
        String sessionId = session.getId();
        if (this.userAuthRepository
                .findEntityByKey(sessionId)
                .filter(auth -> auth.getUserAuthState() == UserAuthState.SUCCESS)
                .isEmpty()) {
            if (!this.userRepository.containsEntityByKey(sessionId)) {
                return;
            }
            logger.warn("Unauthenticated User tried to access room service: {}", sessionId);
            JsonNode errorMessageData = JsonNodeUtils.crateErrorMessageData(
                    sessionId,
                    "unauthenticated user tried to access room service");
            this.messageBroker.sendMessage(sessionId, errorMessageData.toString());
            return;
        }
        String kind = jsonData.get("kind").asText();
        if (kind == null) {
            return;
        }
        if (!kind.startsWith("Room#")) {
            return;
        }
        if (kind.equals("Room#fetchRooms")) {
            fetchRooms(sessionId);
            return;
        }
        if (kind.equals("Room#createRoom")) {
            createRoom(sessionId, jsonData);
            return;
        }
        if (kind.equals("Room#join")) {
            joinRoom(sessionId, jsonData);
            return;
        }
        if (kind.equals("Room#leave")) {
            leaveRoom(sessionId, jsonData);
            return;
        }
        if (kind.equals("Room#sendChat")) {
            sendChat(sessionId, jsonData);
            return;
        }
        JsonNode errorMessageData = JsonNodeUtils.crateErrorMessageData(sessionId, "unknown room service command");
        this.messageBroker.sendMessage(sessionId, errorMessageData.toString());
    }

    public void fetchRooms(String sessionId) {
        ObjectNode jsonData = JsonNodeFactory.instance.objectNode()
                .put("kind", "Room#fetchRooms")
                .put("sessionId", sessionId);
        ArrayNode rooms = jsonData.putArray("rooms");
        rooms.addAll(this.roomRepository
                .getAllEntities()
                .stream()
                .map(room -> JsonNodeFactory.instance.objectNode()
                        .put("roomId", room.getId().toString())
                        .put("name", room.getName()))
                .toList());
        this.messageBroker.sendMessage(sessionId, jsonData.toString());
    }

    public void createRoom(String sessionId, JsonNode jsonData) {
        String roomName = jsonData.get("name").asText();
        if (this.roomRepository.findEntityByName(roomName).isPresent()) {
            JsonNode errorMessageData = JsonNodeUtils.crateErrorMessageData(
                    sessionId,
                    "room already exists");
            this.messageBroker.sendMessage(sessionId, errorMessageData.toString());
            return;
        }
        Room room = new Room(UUID.randomUUID(), roomName, new CopyOnWriteArrayList<>());
        this.roomRepository.addEntity(room);
        JsonNode responseData = JsonNodeFactory.instance.objectNode()
                .put("kind", "Room#createRoom")
                .put("roomId", room.getId().toString())
                .put("name", room.getName());
        this.messageBroker.sendMessage(sessionId, responseData.toString());
    }

    public void joinRoom(String sessionId, JsonNode jsonData) {
        UUID roomId = UUID.fromString(jsonData.get("roomId").asText());
        User user = this.userRepository
                .findEntityByKey(sessionId)
                .orElseThrow();
        Optional<Room> roomOptional = this.roomRepository.findEntityByKey(roomId);
        if (roomOptional.isEmpty()) {
            JsonNode errorMessageData = JsonNodeUtils.crateErrorMessageData(
                    sessionId,
                    "room does not exist");
            this.messageBroker.sendMessage(sessionId, errorMessageData.toString());
            return;
        }
        Room room = roomOptional.get();
        room.getUsers().add(user);
        ObjectNode responseData = JsonNodeFactory.instance.objectNode()
                .put("kind", "Room#join")
                .put("roomId", roomId.toString())
                .put("timestamp", LocalDateTime
                        .now()
                        .format(DateTimeFormatter.ISO_DATE_TIME));
        responseData.putObject("user")
                .put("sessionId", user.getSessionId())
                .put("name", user.getName())
                .put("joinedTime", user
                        .getJoinedTime()
                        .format(DateTimeFormatter.ISO_DATE_TIME));
        for (User u : room.getUsers()) {
            this.messageBroker.sendMessage(u.getSessionId(), responseData.toString());
        }
    }

    public void leaveRoom(String sessionId, JsonNode jsonData) {
        UUID roomId = UUID.fromString(jsonData.get("roomId").asText());
        User user = this.userRepository
                .findEntityByKey(sessionId)
                .orElseThrow();
        Optional<Room> roomOptional = this.roomRepository.findEntityByKey(roomId);
        if (roomOptional.isEmpty()) {
            JsonNode errorMessageData = JsonNodeUtils.crateErrorMessageData(
                    sessionId,
                    "room does not exist");
            this.messageBroker.sendMessage(sessionId, errorMessageData.toString());
            return;
        }
        Room room = roomOptional.get();
        ObjectNode responseData = JsonNodeFactory.instance.objectNode()
                .put("kind", "Room#leave")
                .put("roomId", roomId.toString())
                .put("timestamp", LocalDateTime
                        .now()
                        .format(DateTimeFormatter.ISO_DATE_TIME));
        responseData.putObject("user")
                .put("sessionId", user.getSessionId())
                .put("name", user.getName())
                .put("joinedTime", user
                        .getJoinedTime()
                        .format(DateTimeFormatter.ISO_DATE_TIME));
        for (User u : room.getUsers()) {
            this.messageBroker.sendMessage(u.getSessionId(), responseData.toString());
        }
        room.getUsers().remove(user);
        if (room.getUsers().isEmpty()) {
            this.roomRepository.removeEntity(room);
            logger.info("Room {} removed because no users have joined it.", roomId);
        }
    }

    public void sendChat(String sessionId, JsonNode jsonData) {
        UUID roomId = UUID.fromString(jsonData.get("roomId").asText());
        User user = this.userRepository
                .findEntityByKey(sessionId)
                .orElseThrow();
        Optional<Room> roomOptional = this.roomRepository.findEntityByKey(roomId);
        if (roomOptional.isEmpty()) {
            JsonNode errorMessageData = JsonNodeUtils.crateErrorMessageData(
                    sessionId,
                    "room does not exist");
            this.messageBroker.sendMessage(sessionId, errorMessageData.toString());
            return;
        }
        ObjectNode responseData = JsonNodeFactory.instance.objectNode()
                .put("kind", "Room#sendChat")
                .put("roomId", roomId.toString())
                .put("timestamp", LocalDateTime
                        .now()
                        .format(DateTimeFormatter.ISO_DATE_TIME))
                .put("content", jsonData.get("content").asText());
        responseData.putObject("user")
                .put("sessionId", user.getSessionId())
                .put("name", user.getName())
                .put("joinedTime", user
                        .getJoinedTime()
                        .format(DateTimeFormatter.ISO_DATE_TIME));
        Room room = roomOptional.get();
        for (User u : room.getUsers()) {
            this.messageBroker.sendMessage(u.getSessionId(), responseData.toString());
        }
    }

    @Override
    public void destroy() throws Exception {
        this.webSocketEventListenerManager.removeListener(this);
    }

}
