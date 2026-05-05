package koza.licensemanagementservice.global.redis;

import koza.licensemanagementservice.domain.session.repository.SessionRepositoryImpl;
import koza.licensemanagementservice.sdk.service.SdkService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class RedisKeyExpirationListener extends KeyExpirationEventMessageListener {
    private final SdkService sdkService;
    public RedisKeyExpirationListener(RedisMessageListenerContainer listenerContainer, SdkService sdkService) {
        super(listenerContainer);
        this.sdkService = sdkService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (expiredKey.startsWith(SessionRepositoryImpl.SESSION_TRIGGER_PREFIX)) {
            String sessionId = expiredKey.split(":")[1];
            sdkService.revokeExpire(sessionId);
        }
    }
}
