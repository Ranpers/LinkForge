package io.github.ranpers.linkforge.link.availability.adapter.in.messaging;

import io.github.ranpers.linkforge.link.availability.domain.AuthorizationEvent;
import io.github.ranpers.linkforge.link.availability.domain.AuthorizationEventType;
import io.github.ranpers.linkforge.link.availability.domain.DomainAvailabilityChanged;
import io.github.ranpers.linkforge.link.availability.domain.UserAvailabilityChanged;
import io.github.ranpers.linkforge.link.availability.domain.UserDomainGrantChanged;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

/** v1 扁平事件信封的严格边界解析器。 */
@Component
public class AuthorizationEventJsonParser {

    private final ObjectMapper objectMapper;

    public AuthorizationEventJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AuthorizationEvent parse(String payload) {
        JsonNode root = readObject(payload);
        UUID eventId = uuid(root, "eventId");
        String eventTypeValue = text(root, "eventType");
        String streamKey = text(root, "streamKey");
        long revision = positiveLong(root, "revision");
        OffsetDateTime occurredAt = offsetDateTime(root, "occurredAt");

        AuthorizationEventType eventType;
        try {
            eventType = AuthorizationEventType.fromWireName(eventTypeValue);
        } catch (IllegalArgumentException exception) {
            throw invalid(exception.getMessage(), exception);
        }

        try {
            return switch (eventType) {
                case DOMAIN_AVAILABILITY_CHANGED -> new DomainAvailabilityChanged(
                        eventId,
                        streamKey,
                        revision,
                        occurredAt,
                        uuid(root, "domainId"),
                        bool(root, "enabled")
                );
                case USER_AVAILABILITY_CHANGED -> new UserAvailabilityChanged(
                        eventId,
                        streamKey,
                        revision,
                        occurredAt,
                        uuid(root, "userId"),
                        bool(root, "enabled")
                );
                case USER_DOMAIN_GRANT_CHANGED -> new UserDomainGrantChanged(
                        eventId,
                        streamKey,
                        revision,
                        occurredAt,
                        uuid(root, "userId"),
                        uuid(root, "domainId"),
                        bool(root, "granted")
                );
            };
        } catch (InvalidAuthorizationEventException exception) {
            throw exception;
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw invalid("授权事件字段不一致: " + exception.getMessage(), exception);
        }
    }

    private JsonNode readObject(String payload) {
        if (payload == null || payload.isBlank()) {
            throw invalid("授权事件消息体不能为空");
        }
        try {
            JsonNode root = objectMapper.readTree(payload);
            if (root == null || !root.isObject()) {
                throw invalid("授权事件消息体必须是 JSON 对象");
            }
            return root;
        } catch (InvalidAuthorizationEventException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw invalid("授权事件不是合法 JSON", exception);
        }
    }

    private static String text(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || !value.isString() || value.stringValue().isBlank()) {
            throw invalid("字段必须是非空字符串: " + field);
        }
        return value.stringValue();
    }

    private static UUID uuid(JsonNode root, String field) {
        try {
            return UUID.fromString(text(root, field));
        } catch (IllegalArgumentException exception) {
            throw invalid("字段不是合法 UUID: " + field, exception);
        }
    }

    private static long positiveLong(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || !value.isIntegralNumber() || !value.canConvertToLong()) {
            throw invalid("字段必须是 bigint: " + field);
        }
        long result = value.longValue();
        if (result < 1) {
            throw invalid("字段必须大于 0: " + field);
        }
        return result;
    }

    private static boolean bool(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || !value.isBoolean()) {
            throw invalid("字段必须是 boolean: " + field);
        }
        return value.booleanValue();
    }

    private static OffsetDateTime offsetDateTime(JsonNode root, String field) {
        try {
            return OffsetDateTime.parse(text(root, field));
        } catch (DateTimeParseException exception) {
            throw invalid("字段不是合法 ISO-8601 timestamptz: " + field, exception);
        }
    }

    private static InvalidAuthorizationEventException invalid(String message) {
        return new InvalidAuthorizationEventException(message);
    }

    private static InvalidAuthorizationEventException invalid(String message, Throwable cause) {
        return new InvalidAuthorizationEventException(message, cause);
    }
}
