package com.fredfmelo.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.fredfmelo.eventdrivencore.config.DynamoProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties
public class ServiceConfig implements DynamoProperties {

    private Aws aws;
    private Jwt jwt;

    @Getter
    @Setter
    public static class Aws {
        private DynamoDb dynamodb;
        private Sns sns;
    }

    @Getter
    @Setter
    public static class Jwt{
        private long expirationSeconds;
    }

    @Getter
    @Setter
    public static class DynamoDb {
        private String tableName;
    }

    @Getter
    @Setter
    public static class Sns {
        private String orderTopicArn;
    }

    @Override
    public String tableName() {
        return aws.getDynamodb().getTableName();
    }
    
}