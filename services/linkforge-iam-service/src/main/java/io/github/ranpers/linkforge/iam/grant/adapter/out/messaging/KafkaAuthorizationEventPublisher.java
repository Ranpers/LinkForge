package io.github.ranpers.linkforge.iam.grant.adapter.out.messaging;

import io.github.ranpers.linkforge.iam.grant.application.PendingOutboxEvent;
import io.github.ranpers.linkforge.iam.grant.application.port.out.AuthorizationEventPublisher;
import io.github.ranpers.linkforge.iam.grant.config.OutboxDispatchProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class KafkaAuthorizationEventPublisher implements AuthorizationEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public KafkaAuthorizationEventPublisher(
            KafkaTemplate<String, String> kafkaTemplate,
            OutboxDispatchProperties properties
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = requireTopic(properties.getTopic());
    }

    @Override
    public void publish(PendingOutboxEvent event, Duration timeout) {
        try {
            kafkaTemplate.send(topic, event.partitionKey(), event.payload())
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new KafkaEventPublishException("Kafka 投递等待被中断: eventId=" + event.id(), exception);
        } catch (ExecutionException | TimeoutException exception) {
            throw new KafkaEventPublishException("Kafka 投递失败: eventId=" + event.id(), exception);
        }
    }

    private static String requireTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            throw new IllegalArgumentException("linkforge.outbox.topic 不能为空");
        }
        return topic;
    }
}
