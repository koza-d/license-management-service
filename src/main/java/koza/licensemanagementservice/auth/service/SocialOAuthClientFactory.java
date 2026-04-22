package koza.licensemanagementservice.auth.service;

import koza.licensemanagementservice.auth.dto.SocialProvider;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SocialOAuthClientFactory {
    private final List<SocialOAuthClient> clients;

    public SocialOAuthClient getClient(String providerName) {
        SocialProvider socialProvider = SocialProvider.valueOf(providerName.toUpperCase());
        return clients.stream()
                .filter(client -> client.getProvider() == socialProvider)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));
    }

}
