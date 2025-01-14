package com.example.chat.entity;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Room implements Entity, Serializable {

    private final UUID id;
    private final String name;
    private final List<User> users;

    public Room(UUID id, String name, List<User> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }

    public UUID getId() {
        return this.id;
    }

    public String getNames() {
        return this.name;
    }

    public List<User> getUsers() {
        return this.users;
    }

}
