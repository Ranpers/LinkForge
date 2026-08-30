package io.github.ranpers.linkforge.link.availability.application;

import java.util.Objects;

public record AuthorizationEventHandlingResult(Status status, int updatedLinks) {

    public AuthorizationEventHandlingResult {
        Objects.requireNonNull(status, "status 不能为空");
        if (updatedLinks < 0) {
            throw new IllegalArgumentException("updatedLinks 不能为负数");
        }
        if (status != Status.APPLIED && updatedLinks != 0) {
            throw new IllegalArgumentException("未应用事件不能报告短链更新数");
        }
    }

    public enum Status {
        APPLIED,
        DUPLICATE,
        STALE
    }

    public static AuthorizationEventHandlingResult applied(int updatedLinks) {
        return new AuthorizationEventHandlingResult(Status.APPLIED, updatedLinks);
    }

    public static AuthorizationEventHandlingResult duplicate() {
        return new AuthorizationEventHandlingResult(Status.DUPLICATE, 0);
    }

    public static AuthorizationEventHandlingResult stale() {
        return new AuthorizationEventHandlingResult(Status.STALE, 0);
    }
}
