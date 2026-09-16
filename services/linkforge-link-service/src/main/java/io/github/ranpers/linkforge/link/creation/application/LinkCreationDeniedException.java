package io.github.ranpers.linkforge.link.creation.application;

import java.util.UUID;

public class LinkCreationDeniedException extends RuntimeException {
    private final String reasonCode;
    private final UUID decisionId;

    public LinkCreationDeniedException(String reasonCode, UUID decisionId) {
        super("短链创建被 IAM 拒绝: " + reasonCode);
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
