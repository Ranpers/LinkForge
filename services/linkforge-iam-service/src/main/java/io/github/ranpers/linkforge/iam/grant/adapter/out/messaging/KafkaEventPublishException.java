package io.github.ranpers.linkforge.iam.grant.adapter.out.messaging;

public class KafkaEventPublishException extends RuntimeException {

    public KafkaEventPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
