package com.fredfmelo.authservice.auth.publisher;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.fredfmelo.authservice.config.ServiceConfig;
import com.fredfmelo.eventdrivencore.exception.TechnicalException;
import com.fredfmelo.eventdrivencore.outbox.publisher.OutboxEventPublisher;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

@Component
@RequiredArgsConstructor
public class AuthEventPublisher implements OutboxEventPublisher {

    private static final String EVENT_TYPE = "eventType";
    private static final String DATA_TYPE_STRING = "String";

    private final SnsClient snsClient;
    private final ServiceConfig serviceConfig;

    @Override
    public void publish(String payload, String eventType) {
        try {
            PublishRequest request = PublishRequest.builder()
                    .topicArn(serviceConfig.getAws().getSns().getOrderTopicArn())
                    .message(payload)
                    .messageAttributes(buildAttributes(eventType))
                    .build();

            snsClient.publish(request);
        } catch (SdkException ex) {
            throw new TechnicalException("Error publishing event", ex);
        }
    }

    private Map<String, MessageAttributeValue> buildAttributes(String eventType) {
        return Map.of(EVENT_TYPE,
                MessageAttributeValue.builder()
                        .dataType(DATA_TYPE_STRING)
                        .stringValue(eventType)
                        .build());
    }
}
