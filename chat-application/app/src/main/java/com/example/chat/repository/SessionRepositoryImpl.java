package com.example.chat.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;
import org.springframework.web.socket.WebSocketSession;

@Repository
public final class SessionRepositoryImpl implements SessionRepository {

    private static final Logger logger = LogManager.getLogger(SessionRepositoryImpl.class);

    private final Map<String, WebSocketSession> sessions;

    public SessionRepositoryImpl() {
        this.sessions = new ConcurrentSkipListMap<>();
    }

    @Override
    public long getCount() {
        return this.sessions.size();
    }

    @Override
    public List<WebSocketSession> getAllEntities() {
        return this.sessions
                .values()
                .stream()
                .toList();
    }

    @Override
    public boolean containsEntityByKey(String key) {
        return this.sessions.containsKey(key);
    }

    @Override
    public Optional<WebSocketSession> findEntityByKey(String key) {
        WebSocketSession session = this.sessions.get(key);
        if (session != null) {
            logger.info("Found session with ID: {}", key);
        }
        return Optional.ofNullable(session);
    }

    @Override
    public void addEntity(WebSocketSession entity) {
        String id = entity.getId();
        if (this.sessions.containsKey(id)) {
            return;
        }
        this.sessions.put(id, entity);
        logger.info("Stored session with ID: {}", id);
    }

    @Override
    public void removeEntity(WebSocketSession entity) {
        String id = entity.getId();
        removeEntityByKey(id);
    }

    @Override
    public void removeEntityByKey(String key) {
        if (!this.sessions.containsKey(key)) {
            return;
        }
        this.sessions.remove(key);
        logger.info("Removed session with ID: {}", key);
    }

}
