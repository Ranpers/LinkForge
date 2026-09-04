package io.github.ranpers.linkforge.iam.grant.application;

import io.github.ranpers.linkforge.iam.grant.application.port.in.LinkManagementAction;
import io.github.ranpers.linkforge.iam.grant.application.port.out.AuthorizationDecisionIdGenerator;
import io.github.ranpers.linkforge.iam.grant.application.port.out.LinkManagementAuthorizationQuery;
import io.github.ranpers.linkforge.iam.grant.application.port.out.LinkManagementAuthorizationSnapshot;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LinkManagementAuthorizationServiceTest {

    private final LinkManagementAuthorizationQuery query =
            mock(LinkManagementAuthorizationQuery.class);
    private final AuthorizationDecisionIdGenerator decisionIds =
            mock(AuthorizationDecisionIdGenerator.class);
    private final LinkManagementAuthorizationService service =
            new LinkManagementAuthorizationService(query, decisionIds);
    private final UUID actor = UUID.randomUUID();
    private final UUID creator = UUID.randomUUID();
    private final UUID domain = UUID.randomUUID();

    @Test
    void globalManagerDoesNotNeedCreatorOwnershipOrDomainGrant() {
        when(query.load(actor, domain, creator, LinkManagementAction.UPDATE))
                .thenReturn(new LinkManagementAuthorizationSnapshot(true, true, true, false));

        var decision = service.validate(actor, domain, creator, LinkManagementAction.UPDATE);

        assertTrue(decision.allowed());
        assertEquals("ALLOWED", decision.reasonCode());
    }

    @Test
    void creatorCanManageOnlyWhenOwnManagementFactsAreSatisfied() {
        when(query.load(actor, domain, actor, LinkManagementAction.DELETE))
                .thenReturn(new LinkManagementAuthorizationSnapshot(true, true, false, true));

        assertTrue(service.validate(actor, domain, actor, LinkManagementAction.DELETE).allowed());
    }

    @Test
    void disabledDomainBlocksEvenGlobalManager() {
        when(query.load(actor, domain, creator, LinkManagementAction.UPDATE))
                .thenReturn(new LinkManagementAuthorizationSnapshot(true, false, true, false));

        var decision = service.validate(actor, domain, creator, LinkManagementAction.UPDATE);

        assertFalse(decision.allowed());
        assertEquals("DOMAIN_NOT_AVAILABLE", decision.reasonCode());
    }
}
