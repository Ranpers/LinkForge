package io.github.ranpers.linkforge.link.creation.application;

public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException() {
        super("相同 Idempotency-Key 已用于不同的创建请求");
    }
}
