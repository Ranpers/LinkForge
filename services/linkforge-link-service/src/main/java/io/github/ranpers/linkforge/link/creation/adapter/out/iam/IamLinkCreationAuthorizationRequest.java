package io.github.ranpers.linkforge.link.creation.adapter.out.iam;

import java.util.UUID;

record IamLinkCreationAuthorizationRequest(UUID userId, UUID domainId) {
}
