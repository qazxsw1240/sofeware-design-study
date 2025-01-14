package com.example.chat.repository;

import com.example.chat.entity.UserAuth;

public interface UserAuthRepository extends SessionBasedRepository<UserAuth, String> {

    public SessionRepository getSessionRepository();

    public void updateUserAuth(UserAuth userAuth);

}
