package com.example.chat.repository;

import com.example.chat.entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final Logger logger = LogManager.getLogger(UserRepositoryImpl.class);

    private final SessionRepository sessionRepository;
    private final Map<String, User> users;

    public UserRepositoryImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
        this.users = new ConcurrentSkipListMap<>();
    }

    @Override
    public SessionRepository getSessionRepository() {
        return this.sessionRepository;
    }

    @Override
    public List<User> getAllEntities() {
        return this.users
                .values()
                .stream()
                .toList();
    }

    @Override
    public boolean containsEntityByKey(String key) {
        return this.users.containsKey(key);
    }

    @Override
    public Optional<User> findEntityByKey(String key) {
        User user = this.users.get(key);
        if (user != null) {
            logger.info("Found user with session ID: {}", key);
        }
        return Optional.ofNullable(user);
    }

    @Override
    public void addEntity(User entity) {
        String sessionId = entity.getSessionId();
        if (this.users.containsKey(sessionId)) {
            return;
        }
        this.users.put(sessionId, entity);
        logger.info("Stored user with session ID: {}", sessionId);
    }

    @Override
    public void removeEntity(User entity) {
        String sessionId = entity.getSessionId();
        removeEntityByKey(sessionId);
    }

    @Override
    public void removeEntityByKey(String key) {
        if (!this.users.containsKey(key)) {
            return;
        }
        this.users.remove(key);
        logger.info("Removed user with session ID: {}", key);
    }

}
