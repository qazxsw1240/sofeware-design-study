package com.example.chat.entity;

import java.io.Serializable;

public class UserAuth implements Entity, Serializable {

    private final String sessionId;
    private final UserAuthState userAuthState;

    public UserAuth(String sessionId, UserAuthState userAuthState) {
        this.sessionId = sessionId;
        this.userAuthState = userAuthState;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public UserAuthState getUserAuthState() {
        return this.userAuthState;
    }

    @Override
    public String toString() {
        return String.format("UserAuth[sessionId='%s', userAuthState=%s]", this.sessionId, this.userAuthState);
    }

}
