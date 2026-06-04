package com.fredfmelo.authservice.config;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

@Service
@RequiredArgsConstructor
public class SecretsManagerService {

    private final SecretsManagerClient secretsManagerClient;

    public String getSecret(String secretName) {
        return secretsManagerClient.getSecretValue(
                GetSecretValueRequest.builder()
                        .secretId(secretName)
                        .build())
                .secretString();
    }
}