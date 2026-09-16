package io.github.ranpers.linkforge.iam.grant.adapter.out.id;

import io.github.ranpers.linkforge.iam.grant.application.port.out.AuthorizationDecisionIdGenerator;
import io.github.ranpers.linkforge.iam.infrastructure.id.UuidV7;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidV7AuthorizationDecisionIdGenerator implements AuthorizationDecisionIdGenerator {
    @Override
    public UUID nextId() {
        return UuidV7.generate();
    }
}
