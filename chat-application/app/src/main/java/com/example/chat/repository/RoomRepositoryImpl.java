package com.example.chat.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;

import org.springframework.stereotype.Repository;

import com.example.chat.entity.Room;

@Repository
public class RoomRepositoryImpl implements RoomRepository {

    private final Map<UUID, Room> rooms;

    public RoomRepositoryImpl() {
        this.rooms = new ConcurrentSkipListMap<>();
    }

    @Override
    public long getCount() {
        return this.rooms.size();
    }

    @Override
    public List<Room> getAllEntities() {
        return this.rooms
                .values()
                .stream()
                .toList();
    }

    @Override
    public boolean containsEntityByKey(UUID key) {
        return this.rooms.containsKey(key);
    }

    @Override
    public Optional<Room> findEntityByKey(UUID key) {
        return Optional.ofNullable(this.rooms.get(key));
    }

    @Override
    public void addEntity(Room entity) {
        if (this.rooms.containsKey(entity.getId())) {
            return;
        }
        this.rooms.put(entity.getId(), entity);
    }

    @Override
    public void removeEntity(Room entity) {
        removeEntityByKey(entity.getId());
    }

    @Override
    public void removeEntityByKey(UUID key) {
        if (!this.rooms.containsKey(key)) {
            return;
        }
        this.rooms.remove(key);
    }

    @Override
    public Optional<Room> findEntityByName(String name) {
        return this.rooms
                .values()
                .stream()
                .filter(room -> room.getName().equals(name))
                .findFirst();
    }

}
