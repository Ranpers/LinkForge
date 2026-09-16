package io.github.ranpers.linkforge.link.management.application;

import java.util.UUID;

public class LinkManagementDeniedException extends RuntimeException {
    private final String reasonCode;
    private final UUID decisionId;

    public LinkManagementDeniedException(String reasonCode, UUID decisionId) {
        super("当前用户不能管理该短链");
        this.reasonCode = reasonCode;
        this.decisionId = decisionId;
    }

    public String reasonCode() {
        return reasonCode;
    }

    public UUID decisionId() {
        return decisionId;
    }
}
