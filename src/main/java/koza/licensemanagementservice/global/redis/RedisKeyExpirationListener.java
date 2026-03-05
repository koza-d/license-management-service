package koza.licensemanagementservice.global.redis;

import koza.licensemanagementservice.verification.repository.SessionRepositoryImpl;
import koza.licensemanagementservice.verification.service.VerificationService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    private final VerificationService verificationService;
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer, VerificationService verificationService) {
        super(listenerContainer);
        this.verificationService = verificationService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (expiredKey.startsWith(SessionRepositoryImpl.SESSION_TRIGGER_PREFIX)) {
            String sessionId = expiredKey.split(":")[1];
            verificationService.revokeExpire(sessionId);
        }
    }
}
