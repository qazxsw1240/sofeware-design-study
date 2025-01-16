package com.example.chat.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

import org.springframework.stereotype.Repository;

import com.example.chat.entity.User;

@Repository
public class UserRepositoryImpl
        extends AbstractSessionBasedRepository<User>
        implements UserRepository {

    private final Map<String, User> users;

    public UserRepositoryImpl(SessionRepository sessionRepository) {
        super(sessionRepository, User::getSessionId, User.class);
        this.users = new ConcurrentSkipListMap<>();
    }

    @Override
    public long getCount() {
        return this.users.size();
    }

    @Override
    public List<User> getAllEntities() {
        return this.users
                .values()
                .stream()
                .toList();
    }

    @Override
    protected boolean containsEntityByKeyBasedOnSession(String sessionId) {
        return this.users.containsKey(sessionId);
    }

    @Override
    protected Optional<User> findEntityByKeyBasedOnSession(String sessionId) {
        return Optional.ofNullable(this.users.get(sessionId));
    }

    @Override
    protected boolean addEntityBasedOnSession(User entity) {
        String sessionId = entity.getSessionId();
        if (this.users.containsKey(sessionId)) {
            return false;
        }
        this.users.put(sessionId, entity);
        return true;
    }

    @Override
    protected boolean removeEntityByKeyBasedOnSession(String sessionId) {
        if (!this.users.containsKey(sessionId)) {
            return false;
        }
        this.users.remove(sessionId);
        return true;
    }

}
