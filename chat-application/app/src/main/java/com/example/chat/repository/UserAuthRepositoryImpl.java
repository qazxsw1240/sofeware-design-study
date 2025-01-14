package com.example.chat.repository;

import com.example.chat.entity.UserAuth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

@Repository
public class UserAuthRepositoryImpl implements UserAuthRepository {

    private static final Logger logger = LogManager.getLogger(UserAuthRepositoryImpl.class);

    private final SessionRepository sessionRepository;
    private final Map<String, UserAuth> userAuths;

    public UserAuthRepositoryImpl(SessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
        this.userAuths = new ConcurrentSkipListMap<>();
    }

    @Override
    public SessionRepository getSessionRepository() {
        return this.sessionRepository;
    }

    @Override
    public void updateUserAuth(UserAuth userAuth) {
        String sessionId = userAuth.getSessionId();
        if (!this.userAuths.containsKey(sessionId)) {
            logger.warn("Cannot update user auth with session ID: {}", sessionId);
            return;
        }
        this.userAuths.put(sessionId, userAuth);
        logger.info("User auth updated to {} with session ID: {}", userAuth.getUserAuthState(), sessionId);
    }

    @Override
    public List<UserAuth> getAllEntities() {
        return this.userAuths
                .values()
                .stream()
                .toList();
    }

    @Override
    public boolean containsEntityByKey(String key) {
        if (warnSessionOmission(key)) {
            return false;
        }
        return this.userAuths.containsKey(key);
    }

    @Override
    public Optional<UserAuth> findEntityByKey(String key) {
        if (warnSessionOmission(key)) {
            return Optional.empty();
        }
        UserAuth userAuth = this.userAuths.get(key);
        if (userAuth != null) {
            logger.info("Found user auth with session ID: {}", key);
        }
        return Optional.ofNullable(userAuth);
    }

    @Override
    public void addEntity(UserAuth entity) {
        String sessionId = entity.getSessionId();
        if (warnSessionOmission(sessionId)) {
            return;
        }
        if (this.userAuths.containsKey(sessionId)) {
            return;
        }
        this.userAuths.put(sessionId, entity);
        logger.info("Stored user auth with session ID: {}", sessionId);
    }

    @Override
    public void removeEntity(UserAuth entity) {
        String sessionId = entity.getSessionId();
        this.userAuths.remove(sessionId);
    }

    @Override
    public void removeEntityByKey(String key) {
        if (warnSessionOmission(key)) {
            return;
        }
        if (!this.userAuths.containsKey(key)) {
            return;
        }
        this.userAuths.remove(key);
        logger.info("Removed user auth with session ID: {}", key);
    }

    private boolean warnSessionOmission(String sessionId) {
        boolean isSessionOmitted = !this.sessionRepository.containsEntityByKey(sessionId);
        if (isSessionOmitted) {
            logger.warn("user auth requires session with ID: {}", sessionId);
        }
        return isSessionOmitted;
    }

}
