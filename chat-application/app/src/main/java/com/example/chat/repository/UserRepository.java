package com.example.chat.repository;

import com.example.chat.entity.User;

public interface UserRepository extends EntityRepository<User, String> {

    public SessionRepository getSessionRepository();

}
