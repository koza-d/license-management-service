package koza.licensemanagementservice.global.redis;

import koza.licensemanagementservice.verification.repository.SessionRepositoryImpl;
import koza.licensemanagementservice.verification.service.SessionManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    private final SessionManager sessionManager;
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer, SessionManager sessionManager) {
        super(listenerContainer);
        this.sessionManager = sessionManager;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (expiredKey.startsWith(SessionRepositoryImpl.SESSION_TRIGGER_PREFIX)) {
            String sessionId = expiredKey.split(":")[1];
            sessionManager.revokeExpiredSession(sessionId);
        }
    }
}
