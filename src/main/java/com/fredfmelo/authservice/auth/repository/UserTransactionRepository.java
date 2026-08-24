package com.fredfmelo.authservice.auth.repository;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.fredfmelo.authservice.auth.entity.UserEntity;
import com.fredfmelo.authservice.config.ServiceConfig;
import com.fredfmelo.eventdrivencore.outbox.entity.OutboxEntity;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.Put;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItem;
import software.amazon.awssdk.services.dynamodb.model.TransactWriteItemsRequest;

@Repository
@RequiredArgsConstructor
public class UserTransactionRepository {

    private final DynamoDbClient dynamoDbClient;
    private final ServiceConfig serviceConfig;

    public void save(UserEntity user, OutboxEntity outbox) {
        dynamoDbClient.transactWriteItems(TransactWriteItemsRequest.builder()
                .transactItems(List.of(
                        buildPut(user, UserEntity.class),
                        buildPut(outbox, OutboxEntity.class)))
                .build());
    }

    private <T> TransactWriteItem buildPut(T entity, Class<T> clazz) {
        Map<String, AttributeValue> item = TableSchema.fromBean(clazz).itemToMap(entity, true);

        return TransactWriteItem.builder()
                .put(Put.builder()
                        .tableName(serviceConfig.getAws().getDynamodb().getTableName())
                        .item(item)
                        .build())
                .build();
    }
}
