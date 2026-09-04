package io.github.ranpers.linkforge.link.control.adapter.in.messaging;

import io.github.ranpers.linkforge.link.control.domain.RestrictionMode;
import io.github.ranpers.linkforge.link.control.domain.UserLinkSecurityRestrictionsChanged;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertEquals(RestrictionMode.CREATED_DURING, event.restrictions().getFirst().mode());
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
}
