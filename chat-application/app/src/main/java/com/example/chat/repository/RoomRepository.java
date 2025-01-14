package com.example.chat.repository;

import com.example.chat.entity.Room;

import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends EntityRepository<Room, UUID> {

    public Optional<Room> findEntityByName(String name);

}
