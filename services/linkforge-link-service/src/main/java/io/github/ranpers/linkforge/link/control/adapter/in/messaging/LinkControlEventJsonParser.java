package io.github.ranpers.linkforge.link.control.adapter.in.messaging;

import io.github.ranpers.linkforge.link.control.domain.DomainAvailabilityChanged;
import io.github.ranpers.linkforge.link.control.domain.LinkControlEvent;
import io.github.ranpers.linkforge.link.control.domain.LinkControlEventType;
import io.github.ranpers.linkforge.link.control.domain.LinkSecurityRestriction;
import io.github.ranpers.linkforge.link.control.domain.RestrictionMode;
import io.github.ranpers.linkforge.link.control.domain.UserLinkSecurityRestrictionsChanged;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class LinkControlEventJsonParser {

    private final ObjectMapper objectMapper;

    public LinkControlEventJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public LinkControlEvent parse(String message) {
        JsonNode root = readObject(message);
        UUID eventId = uuid(root, "eventId");
        LinkControlEventType type = eventType(root);
        int schemaVersion = positiveInt(root, "schemaVersion");
        String streamKey = text(root, "streamKey");
        long revision = positiveLong(root, "revision");
        OffsetDateTime occurredAt = dateTime(root, "occurredAt");
        String traceId = optionalText(root, "traceId");
        JsonNode payload = object(root, "payload");

        try {
            return switch (type) {
                case DOMAIN_AVAILABILITY_CHANGED -> new DomainAvailabilityChanged(
                        eventId,
                        schemaVersion,
                        streamKey,
                        revision,
                        occurredAt,
                        traceId,
                        uuid(payload, "domainId"),
                        text(payload, "host"),
                        bool(payload, "enabled")
                );
                case USER_LINK_SECURITY_RESTRICTIONS_CHANGED ->
                        new UserLinkSecurityRestrictionsChanged(
                                eventId,
                                schemaVersion,
                                streamKey,
                                revision,
                                occurredAt,
                                traceId,
                                uuid(payload, "userId"),
                                restrictions(payload)
                        );
            };
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw invalid("Link 控制事件字段不一致: " + exception.getMessage(), exception);
        }
    }

    private static List<LinkSecurityRestriction> restrictions(JsonNode payload) {
        JsonNode array = payload.get("restrictions");
        if (array == null || !array.isArray()) {
            throw invalid("payload.restrictions 必须是数组");
        }
        List<LinkSecurityRestriction> restrictions = new ArrayList<>();
        for (JsonNode item : array) {
            RestrictionMode mode;
            try {
                mode = RestrictionMode.valueOf(text(item, "mode"));
            } catch (IllegalArgumentException exception) {
                throw invalid("未知安全限制模式", exception);
            }
            restrictions.add(new LinkSecurityRestriction(
                    uuid(item, "restrictionId"),
                    mode,
                    optionalDateTime(item, "rangeStart"),
                    optionalDateTime(item, "rangeEnd"),
                    text(item, "reasonCode"),
                    dateTime(item, "createdAt")
            ));
        }
        return List.copyOf(restrictions);
    }

    private JsonNode readObject(String message) {
        if (message == null || message.isBlank()) {
            throw invalid("Link 控制事件不能为空");
        }
        try {
            JsonNode root = objectMapper.readTree(message);
            if (root == null || !root.isObject()) {
                throw invalid("Link 控制事件必须是 JSON 对象");
            }
            return root;
        } catch (InvalidLinkControlEventException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw invalid("Link 控制事件不是合法 JSON", exception);
        }
    }

    private static LinkControlEventType eventType(JsonNode root) {
        try {
            return LinkControlEventType.fromWireName(text(root, "eventType"));
        } catch (IllegalArgumentException exception) {
            throw invalid(exception.getMessage(), exception);
        }
    }

    private static JsonNode object(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || !value.isObject()) {
            throw invalid("字段必须是对象: " + field);
        }
        return value;
    }

    private static String text(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || !value.isString() || value.stringValue().isBlank()) {
            throw invalid("字段必须是非空字符串: " + field);
        }
        return value.stringValue();
    }

    private static String optionalText(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return text(root, field);
    }

    private static UUID uuid(JsonNode root, String field) {
        try {
            return UUID.fromString(text(root, field));
        } catch (IllegalArgumentException exception) {
            throw invalid("字段不是合法 UUID: " + field, exception);
        }
    }

    private static int positiveInt(JsonNode root, String field) {
        long value = positiveLong(root, field);
        if (value > Integer.MAX_VALUE) {
            throw invalid("字段超出 integer 范围: " + field);
        }
        return (int) value;
    }

    private static long positiveLong(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || !value.isIntegralNumber() || !value.canConvertToLong()) {
            throw invalid("字段必须是整数: " + field);
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

    private static OffsetDateTime dateTime(JsonNode root, String field) {
        try {
            return OffsetDateTime.parse(text(root, field));
        } catch (DateTimeParseException exception) {
            throw invalid("字段不是合法 ISO-8601 时间: " + field, exception);
        }
    }

    private static OffsetDateTime optionalDateTime(JsonNode root, String field) {
        JsonNode value = root.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return dateTime(root, field);
    }

    private static InvalidLinkControlEventException invalid(String message) {
        return new InvalidLinkControlEventException(message);
    }

    private static InvalidLinkControlEventException invalid(String message, Throwable cause) {
        return new InvalidLinkControlEventException(message, cause);
    }
}
