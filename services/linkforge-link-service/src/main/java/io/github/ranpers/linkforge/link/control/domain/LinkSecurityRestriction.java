package io.github.ranpers.linkforge.link.control.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

public record LinkSecurityRestriction(
        UUID restrictionId,
        RestrictionMode mode,
        OffsetDateTime rangeStart,
        OffsetDateTime rangeEnd,
        String reasonCode,
        OffsetDateTime createdAt
) {
    public LinkSecurityRestriction {
        Objects.requireNonNull(restrictionId, "restrictionId");
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(reasonCode, "reasonCode");
        Objects.requireNonNull(createdAt, "createdAt");
        if (reasonCode.isBlank() || reasonCode.length() > 64) {
            throw new IllegalArgumentException("reasonCode 非法");
        }
        if (mode == RestrictionMode.ALL && (rangeStart != null || rangeEnd != null)) {
            throw new IllegalArgumentException("ALL 模式不能携带时间范围");
        }
        if (mode == RestrictionMode.CREATED_DURING
                && rangeStart == null && rangeEnd == null) {
            throw new IllegalArgumentException("CREATED_DURING 至少需要一个范围边界");
        }
        if (rangeStart != null && rangeEnd != null && !rangeStart.isBefore(rangeEnd)) {
            throw new IllegalArgumentException("安全限制时间范围必须是左闭右开非空区间");
        }
    }
}
