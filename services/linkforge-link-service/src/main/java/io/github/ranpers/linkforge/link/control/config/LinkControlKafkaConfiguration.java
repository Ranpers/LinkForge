package io.github.ranpers.linkforge.link.control.config;

import io.github.ranpers.linkforge.link.control.adapter.in.messaging.InvalidLinkControlEventException;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LinkControlConsumerProperties.class)
public class LinkControlKafkaConfiguration {

    @Bean
    DefaultErrorHandler linkControlEventErrorHandler(
            KafkaTemplate<String, String> kafkaTemplate,
            LinkControlConsumerProperties properties
    ) {
        properties.validate();
        var recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> new TopicPartition(
                        record.topic() + properties.getDltSuffix(),
                        -1
                )
        );
        var retry = new FixedBackOff(
                properties.getRetryBackoff().toMillis(),
                properties.getMaxAttempts() - 1L
        );
        var errorHandler = new DefaultErrorHandler(recoverer, retry);
        errorHandler.addNotRetryableExceptions(InvalidLinkControlEventException.class);
        return errorHandler;
    }
}
