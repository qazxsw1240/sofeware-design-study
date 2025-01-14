package com.example.chat.repository;

public interface SessionBasedRepository<T, K extends Comparable<K>>
        extends EntityRepository<T, K> {

    public SessionRepository getSessionRepository();

}
