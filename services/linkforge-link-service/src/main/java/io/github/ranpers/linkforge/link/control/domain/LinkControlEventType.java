package io.github.ranpers.linkforge.link.control.domain;

import java.util.Arrays;

public enum LinkControlEventType {
    DOMAIN_AVAILABILITY_CHANGED("DomainAvailabilityChanged"),
    USER_LINK_SECURITY_RESTRICTIONS_CHANGED("UserLinkSecurityRestrictionsChanged");

    private final String wireName;

    LinkControlEventType(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }

    public static LinkControlEventType fromWireName(String value) {
        return Arrays.stream(values())
                .filter(type -> type.wireName.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的 Link 控制事件类型: " + value));
    }
}
