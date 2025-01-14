package com.example.chat.repository;

import java.util.List;
import java.util.Optional;

public interface EntityRepository<T, K extends Comparable<K>> {

    public List<T> getAllEntities();

    public boolean containsEntityByKey(K key);

    public Optional<T> findEntityByKey(K key);

    public void addEntity(T entity);

    public void removeEntity(T entity);

    public void removeEntityByKey(K key);

}
