package com.fredfmelo.authservice.auth.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.fredfmelo.authservice.auth.entity.UserEntity;
import com.fredfmelo.authservice.config.ServiceConfig;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;


@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final ServiceConfig serviceConfig;
    private final DynamoDbEnhancedClient client;

    private DynamoDbTable<UserEntity> table() {
        return client.table(serviceConfig.getAws().getDynamodb().getTableName(),
                TableSchema.fromBean(UserEntity.class));
    }


    public void save(UserEntity entity) {
        table().putItem(entity);
    }

    public UserEntity findByPk(String pk) {
        Key key = Key.builder()
                .partitionValue(UserEntity.USER_PREFIX + pk)
                .sortValue(UserEntity.PROFILE)
                .build();

        return table().getItem(key);
    }

    public Optional<UserEntity> findByEmail(String email) {
        Key key = Key.builder()
                .partitionValue(UserEntity.EMAIL_PREFIX + email)
                .sortValue(UserEntity.PROFILE)
                .build();
    
        return table()
                .index(UserEntity.EMAIL_INDEX)
                .query(r -> r.queryConditional(QueryConditional.keyEqualTo(key)))
                .stream()
                .flatMap(page -> page.items().stream())
                .findFirst();
    }

}