package io.github.ranpers.linkforge.iam.security.application;

import io.github.ranpers.linkforge.iam.control.domain.ControlEventTraceId;
import io.github.ranpers.linkforge.iam.security.application.port.in.CreateLinkSecurityRestrictionCommand;
import io.github.ranpers.linkforge.iam.security.application.port.in.ManageLinkSecurityRestrictionUseCase;
import io.github.ranpers.linkforge.iam.security.application.port.out.LinkSecurityRestrictionStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LinkSecurityRestrictionService implements ManageLinkSecurityRestrictionUseCase {

    private final LinkSecurityRestrictionStore store;

    public LinkSecurityRestrictionService(LinkSecurityRestrictionStore store) {
        this.store = store;
    }

    @Override
    @Transactional
    public UUID create(CreateLinkSecurityRestrictionCommand command) {
        LinkSecurityRestrictionStore.CreateResult result = store.create(command);
        return switch (result.outcome()) {
            case CHANGED -> result.restrictionId();
            case TARGET_NOT_FOUND -> throw new SecurityTargetUserNotFoundException();
            case DENIED -> throw new SecurityDispositionDeniedException();
            case UNCHANGED, RESTRICTION_NOT_FOUND ->
                    throw new IllegalStateException("创建安全限制返回了非法状态: " + result.outcome());
        };
    }

    @Override
    @Transactional
    public void revoke(
            UUID actorUserId,
            UUID targetUserId,
            UUID restrictionId,
            ControlEventTraceId traceId
    ) {
        switch (store.revoke(actorUserId, targetUserId, restrictionId, traceId)) {
            case CHANGED, UNCHANGED -> {
            }
            case TARGET_NOT_FOUND -> throw new SecurityTargetUserNotFoundException();
            case RESTRICTION_NOT_FOUND -> throw new LinkSecurityRestrictionNotFoundException();
            case DENIED -> throw new SecurityDispositionDeniedException();
        }
    }
}
