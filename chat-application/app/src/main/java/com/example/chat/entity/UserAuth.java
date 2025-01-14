package com.example.chat.entity;

public class UserAuth {

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

}
