package io.github.ranpers.linkforge.link.availability.domain;

import java.util.Arrays;

public enum AuthorizationEventType {
    DOMAIN_AVAILABILITY_CHANGED("DomainAvailabilityChanged"),
    USER_AVAILABILITY_CHANGED("UserAvailabilityChanged"),
    USER_DOMAIN_GRANT_CHANGED("UserDomainGrantChanged");

    private final String wireName;

    AuthorizationEventType(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }

    public static AuthorizationEventType fromWireName(String value) {
        return Arrays.stream(values())
                .filter(type -> type.wireName.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的授权事件类型: " + value));
    }
}
