package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

public record LinkManagementAuthorizationRow(
        boolean userEnabled,
        boolean shortDomainEnabled,
        boolean globalManagementAllowed,
        boolean ownManagementAllowed
) {
}
