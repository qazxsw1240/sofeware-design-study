package com.example.chat.repository;

import com.example.chat.entity.User;

public interface UserRepository extends SessionBasedRepository<User> {

    public SessionRepository getSessionRepository();

}
