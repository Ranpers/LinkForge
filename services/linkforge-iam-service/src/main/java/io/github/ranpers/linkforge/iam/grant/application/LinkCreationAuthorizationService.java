package io.github.ranpers.linkforge.iam.grant.application;

import io.github.ranpers.linkforge.iam.grant.application.port.in.LinkCreationAuthorization;
import io.github.ranpers.linkforge.iam.grant.application.port.in.ValidateLinkCreationAuthorizationUseCase;
import io.github.ranpers.linkforge.iam.grant.application.port.out.LinkCreationAuthorizationQuery;
import io.github.ranpers.linkforge.iam.grant.application.port.out.LinkCreationAuthorizationSnapshot;
import io.github.ranpers.linkforge.iam.grant.application.port.out.AuthorizationDecisionIdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

@Service
public class LinkCreationAuthorizationService implements ValidateLinkCreationAuthorizationUseCase {

    private final LinkCreationAuthorizationQuery query;
    private final AuthorizationDecisionIdGenerator decisionIdGenerator;

    public LinkCreationAuthorizationService(
            LinkCreationAuthorizationQuery query,
            AuthorizationDecisionIdGenerator decisionIdGenerator
    ) {
        this.query = query;
        this.decisionIdGenerator = decisionIdGenerator;
    }

    @Override
    @Transactional(readOnly = true)
    public LinkCreationAuthorization validate(UUID userId, UUID domainId) {
        Objects.requireNonNull(userId, "userId 不能为空");
        Objects.requireNonNull(domainId, "domainId 不能为空");
        LinkCreationAuthorizationSnapshot snapshot = query.load(userId, domainId);
        String reasonCode = reasonCode(snapshot);
        return new LinkCreationAuthorization(
                userId,
                domainId,
                "ALLOWED".equals(reasonCode),
                reasonCode,
                decisionIdGenerator.nextId(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    private static String reasonCode(LinkCreationAuthorizationSnapshot snapshot) {
        if (!snapshot.userEnabled()) {
            return "USER_NOT_AVAILABLE";
        }
        if (!snapshot.actionAllowed()) {
            return "ACTION_NOT_ALLOWED";
        }
        if (!snapshot.domainEnabled()) {
            return "DOMAIN_NOT_AVAILABLE";
        }
        if (!snapshot.domainGranted()) {
            return "DOMAIN_NOT_GRANTED";
        }
        return "ALLOWED";
    }
}
