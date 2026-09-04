package io.github.ranpers.linkforge.iam.security.domain;

import java.time.OffsetDateTime;
import java.util.Objects;

public record LinkSecurityRestriction(
        RestrictionMode mode,
        OffsetDateTime rangeStart,
        OffsetDateTime rangeEnd,
        String reasonCode
) {
    public LinkSecurityRestriction {
        Objects.requireNonNull(mode, "mode");
        Objects.requireNonNull(reasonCode, "reasonCode");
        if (reasonCode.isBlank() || reasonCode.length() > 64) {
            throw new InvalidLinkSecurityRestrictionException(
                    "reasonCode 必须是 1 到 64 个字符"
            );
        }
        if (mode == RestrictionMode.ALL && (rangeStart != null || rangeEnd != null)) {
            throw new InvalidLinkSecurityRestrictionException("ALL 模式不能携带时间范围");
        }
        if (mode == RestrictionMode.CREATED_DURING
                && rangeStart == null && rangeEnd == null) {
            throw new InvalidLinkSecurityRestrictionException(
                    "CREATED_DURING 至少需要一个时间边界"
            );
        }
        if (rangeStart != null && rangeEnd != null && !rangeStart.isBefore(rangeEnd)) {
            throw new InvalidLinkSecurityRestrictionException(
                    "安全限制时间范围必须是左闭右开的非空区间"
            );
        }
    }
}
