package com.example.chat.repository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public abstract class AbstractSessionBasedRepository<T> implements SessionBasedRepository<T> {

    protected final SessionRepository sessionRepository;
    protected final Function<T, String> sessionIdExtractor;
    protected final String entityName;

    protected AbstractSessionBasedRepository(
            SessionRepository sessionRepository,
            Function<T, String> sessionIdExtractor,
            Class<T> entityClass) {
        this.sessionRepository = sessionRepository;
        this.sessionIdExtractor = sessionIdExtractor;
        this.entityName = entityClass.getSimpleName();
    }

    @Override
    public SessionRepository getSessionRepository() {
        return this.sessionRepository;
    }

    @Override
    public boolean containsEntityByKey(String sessionId) {
        if (warnSessionOmission(sessionId)) {
            return false;
        }
        return containsEntityByKeyBasedOnSession(sessionId);
    }

    @Override
    public Optional<T> findEntityByKey(String sessionId) {
        if (warnSessionOmission(sessionId)) {
            return Optional.empty();
        }
        Optional<T> entity = findEntityByKeyBasedOnSession(sessionId);
        if (entity.isPresent()) {
            getLogger().info("Found {} by session ID: {}", this.entityName, sessionId);
        }
        return entity;
    }

    @Override
    public void addEntity(T entity) {
        String sessionId = this.sessionIdExtractor.apply(entity);
        if (warnSessionOmission(sessionId)) {
            return;
        }
        if (addEntityBasedOnSession(entity)) {
            getLogger().info("Stored {} with session ID: {}", this.entityName, sessionId);
        }
    }

    @Override
    public void removeEntity(T entity) {
        String sessionId = this.sessionIdExtractor.apply(entity);
        removeEntityByKey(sessionId);
    }

    @Override
    public void removeEntityByKey(String key) {
        if (warnSessionOmission(key)) {
            return;
        }
        if (removeEntityByKeyBasedOnSession(key)) {
            getLogger().info("Removed {} with session ID: {}", this.entityName, key);
        }
    }

    @Override
    public abstract List<T> getAllEntities();

    protected abstract boolean containsEntityByKeyBasedOnSession(String sessionId);

    protected abstract Optional<T> findEntityByKeyBasedOnSession(String sessionId);

    protected abstract boolean addEntityBasedOnSession(T entity);

    protected abstract boolean removeEntityByKeyBasedOnSession(String sessionId);

    protected Logger getLogger() {
        return LogManager.getLogger(this.getClass());
    }

    private boolean warnSessionOmission(String sessionId) {
        boolean isSessionOmitted = !this.sessionRepository.containsEntityByKey(sessionId);
        if (isSessionOmitted) {
            getLogger().warn("{} requires session with ID: {}", this.entityName, sessionId);
        }
        return isSessionOmitted;
    }

}
