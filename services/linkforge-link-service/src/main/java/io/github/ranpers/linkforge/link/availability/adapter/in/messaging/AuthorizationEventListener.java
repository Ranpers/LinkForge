package io.github.ranpers.linkforge.link.availability.adapter.in.messaging;

import io.github.ranpers.linkforge.link.availability.application.AuthorizationEventHandler;
import io.github.ranpers.linkforge.link.availability.application.AuthorizationEventHandlingResult;
import io.github.ranpers.linkforge.link.availability.domain.AuthorizationEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Kafka 仅负责传输；契约校验后由应用服务执行数据库事务。 */
@Component
public class AuthorizationEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthorizationEventListener.class);

    private final AuthorizationEventJsonParser parser;
    private final AuthorizationEventHandler handler;

    public AuthorizationEventListener(
            AuthorizationEventJsonParser parser,
            AuthorizationEventHandler handler
    ) {
        this.parser = parser;
        this.handler = handler;
    }

    @KafkaListener(
            topics = "${linkforge.authorization-consumer.topic}",
            groupId = "${linkforge.authorization-consumer.group-id}",
            concurrency = "${linkforge.authorization-consumer.concurrency}",
            autoStartup = "${linkforge.authorization-consumer.enabled}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        AuthorizationEvent event = parser.parse(record.value());
        if (!event.partitionKey().equals(record.key())) {
            throw new InvalidAuthorizationEventException(
                    "Kafka key 与事件分区契约不一致: eventId=" + event.eventId()
            );
        }

        AuthorizationEventHandlingResult result = handler.handle(event);
        log.debug(
                "授权事件处理完成: eventId={}, streamKey={}, revision={}, status={}, updatedLinks={}",
                event.eventId(),
                event.streamKey(),
                event.revision(),
                result.status(),
                result.updatedLinks()
        );
    }
}
