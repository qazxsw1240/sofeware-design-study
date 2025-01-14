package com.example.chat.repository;

import com.example.chat.entity.UserAuth;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;

@Repository
public class UserAuthRepositoryImpl
        extends AbstractSessionBasedRepository<UserAuth>
        implements UserAuthRepository {

    private final Map<String, UserAuth> userAuths;

    public UserAuthRepositoryImpl(SessionRepository sessionRepository) {
        super(sessionRepository, UserAuth::getSessionId, UserAuth.class);
        this.userAuths = new ConcurrentSkipListMap<>();
    }

    @Override
    public void updateUserAuth(UserAuth userAuth) {
        String sessionId = userAuth.getSessionId();
        if (!this.userAuths.containsKey(sessionId)) {
            getLogger().warn("Cannot update user auth with session ID: {}", sessionId);
            return;
        }
        this.userAuths.put(sessionId, userAuth);
        getLogger().info("{} updated to {} with session ID: {}", this.entityName, userAuth.getUserAuthState(), sessionId);
    }

    @Override
    public long getCount() {
        return this.userAuths.size();
    }

    @Override
    public List<UserAuth> getAllEntities() {
        return this.userAuths
                .values()
                .stream()
                .toList();
    }

    @Override
    protected boolean containsEntityByKeyBasedOnSession(String sessionId) {
        return this.userAuths.containsKey(sessionId);
    }

    @Override
    protected Optional<UserAuth> findEntityByKeyBasedOnSession(String sessionId) {
        return Optional.ofNullable(this.userAuths.get(sessionId));
    }

    @Override
    protected boolean addEntityBasedOnSession(UserAuth entity) {
        String sessionId = entity.getSessionId();
        if (this.userAuths.containsKey(sessionId)) {
            return false;
        }
        this.userAuths.put(sessionId, entity);
        return true;
    }

    @Override
    protected boolean removeEntityByKeyBasedOnSession(String sessionId) {
        if (!this.userAuths.containsKey(sessionId)) {
            return false;
        }
        this.userAuths.remove(sessionId);
        return true;
    }

}
