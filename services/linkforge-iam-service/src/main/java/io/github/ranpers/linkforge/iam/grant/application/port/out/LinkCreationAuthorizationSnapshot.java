package io.github.ranpers.linkforge.iam.grant.application.port.out;

public record LinkCreationAuthorizationSnapshot(
        boolean userEnabled,
        boolean actionAllowed,
        boolean domainEnabled,
        boolean domainGranted
) {
}
