package com.example.chat.repository;

import java.util.Optional;

public interface SessionBasedRepository<T> extends EntityRepository<T, String> {

    public SessionRepository getSessionRepository();

    @Override
    public boolean containsEntityByKey(String sessionId);

    @Override
    public Optional<T> findEntityByKey(String sessionId);

    @Override
    public void addEntity(T entity);

    @Override
    public void removeEntity(T entity);

    @Override
    public void removeEntityByKey(String sessionId);

}
