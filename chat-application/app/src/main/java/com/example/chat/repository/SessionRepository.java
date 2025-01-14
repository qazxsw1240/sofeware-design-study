package com.example.chat.repository;

import org.springframework.web.socket.WebSocketSession;

public interface SessionRepository extends EntityRepository<WebSocketSession, String> {
}
