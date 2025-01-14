package com.example.chat.repository;

import java.util.Optional;

public interface SessionBasedRepository<T> extends EntityRepository<T, String> {

    public SessionRepository getSessionRepository();

    public boolean containsEntityByKey(String sessionId);

    public Optional<T> findEntityByKey(String sessionId);

    public void addEntity(T entity);

    public void removeEntity(T entity);

    public void removeEntityByKey(String sessionId);

}
