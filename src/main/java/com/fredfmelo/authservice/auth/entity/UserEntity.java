package com.fredfmelo.authservice.auth.entity;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Getter
@Setter
@NoArgsConstructor
@DynamoDbBean
public class UserEntity {

    public static final String USER_PREFIX = "USER#";
    public static final String EMAIL_PREFIX = "EMAIL#";
    public static final String PROFILE = "PROFILE";

    public static final String EMAIL_INDEX = "EMAIL_INDEX";

    private String pk;
    private String sk;

    private String emailPk;
    private String emailSk;

    private UUID userId;
    private String email;
    private String passwordHash;
    private Role role;
    private Instant createdAt;

    @DynamoDbPartitionKey
    public String getPk() {
        return pk;
    }

    @DynamoDbSortKey
    public String getSk() {
        return sk;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = EMAIL_INDEX)
    public String getEmailPk() {
        return emailPk;
    }

    @DynamoDbSecondarySortKey(indexNames = EMAIL_INDEX)
    public String getEmailSk() {
        return emailSk;
    }
}