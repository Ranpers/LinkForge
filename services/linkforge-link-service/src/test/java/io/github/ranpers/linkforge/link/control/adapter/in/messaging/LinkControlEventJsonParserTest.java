package io.github.ranpers.linkforge.link.control.adapter.in.messaging;

import io.github.ranpers.linkforge.link.control.domain.RestrictionMode;
import io.github.ranpers.linkforge.link.control.domain.UserLinkSecurityRestrictionsChanged;
import io.github.ranpers.linkforge.link.control.domain.ControlEventTraceId;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LinkControlEventJsonParserTest {

    private final LinkControlEventJsonParser parser =
            new LinkControlEventJsonParser(JsonMapper.builder().findAndAddModules().build());

    @Test
    void parsesFullSecurityRestrictionSnapshot() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID restrictionId = UUID.randomUUID();
        String json = """
                {
                  "eventId": "%s",
                  "eventType": "UserLinkSecurityRestrictionsChanged",
                  "schemaVersion": 1,
                  "streamKey": "USER_LINK_SECURITY:%s",
                  "revision": 7,
                  "occurredAt": "2026-09-04T00:00:00Z",
                  "traceId": "trace",
                  "payload": {
                    "userId": "%s",
                    "restrictions": [{
                      "restrictionId": "%s",
                      "mode": "CREATED_DURING",
                      "rangeStart": "2026-09-01T00:00:00Z",
                      "rangeEnd": null,
                      "reasonCode": "ACCOUNT_COMPROMISED",
                      "createdAt": "2026-09-04T00:00:00Z"
                    }]
                  }
                }
                """.formatted(eventId, userId, userId, restrictionId);

        UserLinkSecurityRestrictionsChanged event =
                (UserLinkSecurityRestrictionsChanged) parser.parse(json);

        assertEquals(7, event.revision());
        assertEquals(userId, event.userId());
        assertEquals("trace", event.traceId().value());
        assertEquals(RestrictionMode.CREATED_DURING, event.restrictions().getFirst().mode());
    }

    @Test
    void acceptsMissingAndNullTraceId() {
        assertNull(parser.parse(domainEventJson(null)).traceId());
        assertNull(parser.parse(domainEventJsonWithNullTraceId()).traceId());
    }

    @Test
    void rejectsBlankAndOversizedTraceId() {
        assertThrows(
                InvalidLinkControlEventException.class,
                () -> parser.parse(domainEventJson("   "))
        );
        assertThrows(
                InvalidLinkControlEventException.class,
                () -> parser.parse(domainEventJson(
                        "a".repeat(ControlEventTraceId.MAX_LENGTH + 1)
                ))
        );
    }

    @Test
    void delegatesTraceIdBlankSemanticsToTheWireDomainType() {
        assertThrows(
                InvalidLinkControlEventException.class,
                () -> parser.parse(domainEventJson("\u00A0"))
        );
        assertEquals(
                "\u001C",
                parser.parse(domainEventJson("\\u001C")).traceId().value()
        );
        assertEquals(
                " \u00A0a\u3000 ",
                parser.parse(domainEventJson(" \u00A0a\u3000 ")).traceId().value()
        );
    }

    @Test
    void rejectsStreamKeyThatDoesNotMatchPayloadIdentity() {
        UUID userId = UUID.randomUUID();
        String json = """
                {
                  "eventId": "%s",
                  "eventType": "UserLinkSecurityRestrictionsChanged",
                  "schemaVersion": 1,
                  "streamKey": "USER_LINK_SECURITY:%s",
                  "revision": 1,
                  "occurredAt": "2026-09-04T00:00:00Z",
                  "payload": {"userId": "%s", "restrictions": []}
                }
                """.formatted(UUID.randomUUID(), UUID.randomUUID(), userId);

        assertThrows(InvalidLinkControlEventException.class, () -> parser.parse(json));
    }

    private static String domainEventJson(String traceId) {
        String traceField = traceId == null
                ? ""
                : "\"traceId\": \"" + traceId + "\",";
        return domainEventJsonWithTraceField(traceField);
    }

    private static String domainEventJsonWithNullTraceId() {
        return domainEventJsonWithTraceField("\"traceId\": null,");
    }

    private static String domainEventJsonWithTraceField(String traceField) {
        UUID domainId = UUID.randomUUID();
        return """
                {
                  "eventId": "%s",
                  "eventType": "DomainAvailabilityChanged",
                  "schemaVersion": 1,
                  "streamKey": "DOMAIN:%s",
                  "revision": 1,
                  "occurredAt": "2026-09-04T00:00:00Z",
                  %s
                  "payload": {
                    "domainId": "%s",
                    "host": "go.example.com",
                    "enabled": true
                  }
                }
                """.formatted(UUID.randomUUID(), domainId, traceField, domainId);
    }
}
