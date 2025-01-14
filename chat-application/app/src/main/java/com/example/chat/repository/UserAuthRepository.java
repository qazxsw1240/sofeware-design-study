package com.example.chat.repository;

import com.example.chat.entity.UserAuth;

public interface UserAuthRepository extends EntityRepository<UserAuth, String> {

    public SessionRepository getSessionRepository();

    public void updateUserAuth(UserAuth userAuth);

}
