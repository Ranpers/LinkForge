package io.github.ranpers.linkforge.link.availability.adapter.in.messaging;

import io.github.ranpers.linkforge.link.availability.domain.DomainAvailabilityChanged;
import io.github.ranpers.linkforge.link.availability.domain.UserAvailabilityChanged;
import io.github.ranpers.linkforge.link.availability.domain.UserDomainGrantChanged;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthorizationEventJsonParserTest {

    private final AuthorizationEventJsonParser parser = new AuthorizationEventJsonParser(new ObjectMapper());

    @Test
    void shouldParseAllV1EventTypes() {
        UUID userId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();

        var domain = assertInstanceOf(DomainAvailabilityChanged.class, parser.parse("""
                {"eventId":"%s","eventType":"DomainAvailabilityChanged",\
                "streamKey":"DOMAIN:%s","revision":2,"occurredAt":"2026-08-30T10:00:00+08:00",\
                "domainId":"%s","enabled":false}
                """.formatted(UUID.randomUUID(), domainId, domainId)));
        var user = assertInstanceOf(UserAvailabilityChanged.class, parser.parse("""
                {"eventId":"%s","eventType":"UserAvailabilityChanged",\
                "streamKey":"USER:%s","revision":3,"occurredAt":"2026-08-30T10:00:00+08:00",\
                "userId":"%s","enabled":true}
                """.formatted(UUID.randomUUID(), userId, userId)));
        var grant = assertInstanceOf(UserDomainGrantChanged.class, parser.parse("""
                {"eventId":"%s","eventType":"UserDomainGrantChanged",\
                "streamKey":"USER_DOMAIN:%s:%s","revision":4,"occurredAt":"2026-08-30T10:00:00+08:00",\
                "userId":"%s","domainId":"%s","granted":false}
                """.formatted(UUID.randomUUID(), userId, domainId, userId, domainId)));

        assertEquals(2, domain.revision());
        assertEquals(3, user.revision());
        assertEquals(4, grant.revision());
    }

    @Test
    void shouldRejectMalformedEnvelopeAndInconsistentStreamKey() {
        UUID domainId = UUID.randomUUID();
        assertThrows(InvalidAuthorizationEventException.class, () -> parser.parse("not-json"));
        assertThrows(InvalidAuthorizationEventException.class, () -> parser.parse("""
                {"eventId":"%s","eventType":"DomainAvailabilityChanged",\
                "streamKey":"DOMAIN:%s","revision":0,"occurredAt":"not-time",\
                "domainId":"%s","enabled":"false"}
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), domainId)));
        assertThrows(InvalidAuthorizationEventException.class, () -> parser.parse("""
                {"eventId":"%s","eventType":"DomainAvailabilityChanged",\
                "streamKey":"DOMAIN:%s","revision":1,"occurredAt":"2026-08-30T10:00:00+08:00",\
                "domainId":"%s","enabled":false}
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), domainId)));
    }
}
