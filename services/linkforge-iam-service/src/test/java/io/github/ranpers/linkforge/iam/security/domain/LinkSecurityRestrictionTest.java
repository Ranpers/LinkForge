package io.github.ranpers.linkforge.iam.security.domain;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinkSecurityRestrictionTest {

    @Test
    void allModeMustNotHaveRange() {
        assertThrows(IllegalArgumentException.class, () -> new LinkSecurityRestriction(
                RestrictionMode.ALL,
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                null,
                "ACCOUNT_COMPROMISED"
        ));
    }

    @Test
    void createdDuringSupportsOpenEndedRange() {
        assertDoesNotThrow(() -> new LinkSecurityRestriction(
                RestrictionMode.CREATED_DURING,
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                null,
                "ACCOUNT_COMPROMISED"
        ));
    }

    @Test
    void createdDuringRejectsEmptyOrReversedRange() {
        assertThrows(IllegalArgumentException.class, () -> new LinkSecurityRestriction(
                RestrictionMode.CREATED_DURING,
                null,
                null,
                "ACCOUNT_COMPROMISED"
        ));
        assertThrows(IllegalArgumentException.class, () -> new LinkSecurityRestriction(
                RestrictionMode.CREATED_DURING,
                OffsetDateTime.parse("2026-01-02T00:00:00Z"),
                OffsetDateTime.parse("2026-01-01T00:00:00Z"),
                "ACCOUNT_COMPROMISED"
        ));
    }
}
