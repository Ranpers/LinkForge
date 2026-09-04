package io.github.ranpers.linkforge.iam.grant.application.port.out;

public record LinkManagementAuthorizationSnapshot(
        boolean userEnabled,
        boolean domainEnabled,
        boolean globalManagementAllowed,
        boolean ownManagementAllowed
) {
}
