package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

public record LinkManagementAuthorizationRow(
        boolean userEnabled,
        boolean domainEnabled,
        boolean globalManagementAllowed,
        boolean ownManagementAllowed
) {
}
