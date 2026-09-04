package io.github.ranpers.linkforge.link.control.adapter.in.messaging;

import io.github.ranpers.linkforge.link.control.application.LinkControlEventHandler;
import io.github.ranpers.linkforge.link.control.application.LinkControlEventHandlingResult;
import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LinkControlEventListener {

    private static final Logger log = LoggerFactory.getLogger(LinkControlEventListener.class);

    private final LinkControlEventJsonParser parser;
    private final LinkControlEventHandler handler;

    public LinkControlEventListener(
            LinkControlEventJsonParser parser,
            LinkControlEventHandler handler
    ) {
        this.parser = parser;
        this.handler = handler;
    }

    @KafkaListener(
            topics = "${linkforge.link-control-consumer.topic}",
            groupId = "${linkforge.link-control-consumer.group-id}",
            concurrency = "${linkforge.link-control-consumer.concurrency}",
            autoStartup = "${linkforge.link-control-consumer.enabled}"
    )
    public void onMessage(ConsumerRecord<String, String> record) {
        LinkControlEvent event = parser.parse(record.value());
        if (!event.partitionKey().equals(record.key())) {
            throw new InvalidLinkControlEventException(
                    "Kafka key 与 Link 控制事件不一致: eventId=" + event.eventId()
            );
        }
        LinkControlEventHandlingResult result = handler.handle(event);
        log.debug(
                "Link 控制事件处理完成: eventId={}, streamKey={}, revision={}, status={}, changedRows={}",
                event.eventId(),
                event.streamKey(),
                event.revision(),
                result.status(),
                result.changedRows()
        );
    }
}
