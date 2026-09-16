package io.github.ranpers.linkforge.iam.grant.adapter.out.persistence;

public record LinkCreationAuthorizationRow(
        boolean userEnabled,
        boolean actionAllowed,
        boolean domainEnabled,
        boolean domainGranted
) {
}
