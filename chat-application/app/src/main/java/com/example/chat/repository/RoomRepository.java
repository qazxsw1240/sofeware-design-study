package com.example.chat.repository;

import java.util.Optional;
import java.util.UUID;

import com.example.chat.entity.Room;

public interface RoomRepository extends EntityRepository<Room, UUID> {

    public Optional<Room> findEntityByName(String name);

}
