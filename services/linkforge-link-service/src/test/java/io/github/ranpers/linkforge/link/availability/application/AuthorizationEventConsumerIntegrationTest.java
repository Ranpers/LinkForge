package io.github.ranpers.linkforge.link.availability.application;

import io.github.ranpers.linkforge.link.availability.domain.UserDomainGrantChanged;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "linkforge.authorization-consumer.enabled=true",
        "linkforge.authorization-consumer.concurrency=3",
        "linkforge.authorization-consumer.retry-backoff=50ms",
        "linkforge.authorization-consumer.max-attempts=3"
})
class AuthorizationEventConsumerIntegrationTest {

    private static final String TOPIC = "linkforge.iam.authorization.v1";
    private static final String DLT = TOPIC + ".DLT";

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer(
            DockerImageName.parse("postgres:18-alpine")
    );

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            "apache/kafka-native:4.1.2"
    );

    @DynamicPropertySource
    static void infrastructureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("linkforge.authorization-consumer.topic", () -> TOPIC);
        registry.add(
                "linkforge.authorization-consumer.group-id",
                () -> "link-authorization-integration"
        );
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AuthorizationEventHandler handler;

    @Test
    void shouldComposeAndRestoreThreeEventOwnedBitsWithoutTouchingManualBit() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();
        UUID otherDomainId = UUID.randomUUID();
        UUID target = insertLink(userId, domainId, 1);
        UUID sameDomain = insertLink(otherUserId, domainId, 0);
        UUID sameUser = insertLink(userId, otherDomainId, 0);
        UUID unrelated = insertLink(otherUserId, otherDomainId, 0);

        send(domainEvent(UUID.randomUUID(), domainId, 1, false), domainId.toString());
        send(userEvent(UUID.randomUUID(), userId, 1, false), userId.toString());
        send(grantEvent(UUID.randomUUID(), userId, domainId, 1, false), domainId.toString());

        await(Duration.ofSeconds(15), () -> reason(target) == 15);
        assertEquals(2, reason(sameDomain));
        assertEquals(8, reason(sameUser));
        assertEquals(0, reason(unrelated));
        assertEquals(1L, checkpoint("DOMAIN:" + domainId));
        assertEquals(1L, checkpoint("USER:" + userId));
        assertEquals(1L, checkpoint("USER_DOMAIN:" + userId + ":" + domainId));

        send(grantEvent(UUID.randomUUID(), userId, domainId, 2, true), domainId.toString());
        send(userEvent(UUID.randomUUID(), userId, 2, true), userId.toString());
        send(domainEvent(UUID.randomUUID(), domainId, 2, true), domainId.toString());

        await(Duration.ofSeconds(15), () -> reason(target) == 1);
        assertEquals(0, reason(sameDomain));
        assertEquals(0, reason(sameUser));
        assertEquals(0, reason(unrelated));
    }

    @Test
    void shouldDeduplicateEventIdAndRejectLateRevision() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();
        UUID linkId = insertLink(userId, domainId, 0);
        UUID revokeId = UUID.randomUUID();
        String streamKey = "USER_DOMAIN:" + userId + ":" + domainId;
        String revoke = grantEvent(revokeId, userId, domainId, 2, false);

        send(revoke, domainId.toString());
        send(revoke, domainId.toString());
        await(Duration.ofSeconds(15), () -> reason(linkId) == 4 && checkpoint(streamKey) == 2);

        UUID staleId = UUID.randomUUID();
        send(grantEvent(staleId, userId, domainId, 1, true), domainId.toString());
        await(Duration.ofSeconds(15), () -> inboxCount(revokeId, staleId) == 2);

        assertEquals(4, reason(linkId));
        assertEquals(2L, checkpoint(streamKey));
        assertEquals(1, count("SELECT count(*) FROM t_inbox_event WHERE event_id = ?", revokeId));
    }

    @Test
    void shouldUpdateLargeDomainProjectionWithSetBasedSql() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();
        String marker = UUID.randomUUID().toString();
        int linkCount = 1_200;
        jdbc.update(
                """
                INSERT INTO t_link (user_id, name, link_code, full_url, domain_id)
                SELECT ?, 'bulk', 'bulk-' || ? || '-' || sequence_number,
                       'https://example.test/' || sequence_number, ?
                FROM generate_series(1, ?) AS sequence_number
                """,
                userId,
                marker,
                domainId,
                linkCount
        );

        send(domainEvent(UUID.randomUUID(), domainId, 1, false), domainId.toString());
        await(Duration.ofSeconds(15), () -> count(
                """
                SELECT count(*)
                FROM t_link
                WHERE domain_id = ? AND (disabled_reason & 2) != 0
                """,
                domainId
        ) == linkCount);

        send(domainEvent(UUID.randomUUID(), domainId, 2, true), domainId.toString());
        await(Duration.ofSeconds(15), () -> count(
                """
                SELECT count(*)
                FROM t_link
                WHERE domain_id = ? AND (disabled_reason & 2) != 0
                """,
                domainId
        ) == 0);
        assertEquals(2L, checkpoint("DOMAIN:" + domainId));
    }

    @Test
    void shouldRollBackInboxCheckpointAndProjectionTogether() {
        UUID userId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();
        UUID linkId = insertLink(userId, domainId, 0);
        UUID eventId = UUID.randomUUID();
        String streamKey = "USER_DOMAIN:" + userId + ":" + domainId;
        jdbc.execute("""
                ALTER TABLE t_link
                ADD CONSTRAINT ck_test_reject_grant_bit_%s
                CHECK (id <> '%s' OR (disabled_reason & 4) = 0)
                """.formatted(eventId.toString().replace("-", ""), linkId));
        var event = new UserDomainGrantChanged(
                eventId,
                streamKey,
                1,
                OffsetDateTime.now(),
                userId,
                domainId,
                false
        );

        assertThrows(DataIntegrityViolationException.class, () -> handler.handle(event));

        assertEquals(0, reason(linkId));
        assertEquals(0, count("SELECT count(*) FROM t_inbox_event WHERE event_id = ?", eventId));
        assertEquals(0, count(
                "SELECT count(*) FROM t_stream_checkpoint WHERE stream_key = ?",
                streamKey
        ));
    }

    @Test
    void shouldSendPermanentPartitionContractViolationToDlt() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID domainId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        String payload = grantEvent(eventId, userId, domainId, 1, false);

        send(payload, UUID.randomUUID().toString());

        ConsumerRecord<String, String> deadLetter = consumeOne(DLT, Duration.ofSeconds(20));
        assertNotNull(deadLetter);
        assertEquals(payload, deadLetter.value());
        assertEquals(0, count("SELECT count(*) FROM t_inbox_event WHERE event_id = ?", eventId));
    }

    private void send(String payload, String key) throws Exception {
        kafkaTemplate.send(TOPIC, key, payload).get();
    }

    private UUID insertLink(UUID userId, UUID domainId, int disabledReason) {
        UUID id = UUID.randomUUID();
        jdbc.update(
                """
                INSERT INTO t_link
                    (id, user_id, name, link_code, full_url, domain_id, disabled_reason)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                userId,
                "integration",
                "code-" + id,
                "https://example.test/" + id,
                domainId,
                disabledReason
        );
        return id;
    }

    private int reason(UUID linkId) {
        Integer value = jdbc.queryForObject(
                "SELECT disabled_reason FROM t_link WHERE id = ?",
                Integer.class,
                linkId
        );
        return value == null ? -1 : value;
    }

    private long checkpoint(String streamKey) {
        Long value = jdbc.query(
                "SELECT last_applied_revision FROM t_stream_checkpoint WHERE stream_key = ?",
                resultSet -> resultSet.next() ? resultSet.getLong(1) : 0L,
                streamKey
        );
        return value == null ? 0L : value;
    }

    private int inboxCount(UUID first, UUID second) {
        return count(
                "SELECT count(*) FROM t_inbox_event WHERE event_id IN (?, ?)",
                first,
                second
        );
    }

    private int count(String sql, Object... args) {
        Integer value = jdbc.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    private static String domainEvent(UUID eventId, UUID domainId, long revision, boolean enabled) {
        return """
                {"eventId":"%s","eventType":"DomainAvailabilityChanged",\
                "streamKey":"DOMAIN:%s","revision":%d,"occurredAt":"%s",\
                "domainId":"%s","enabled":%s}
                """.formatted(eventId, domainId, revision, OffsetDateTime.now(), domainId, enabled);
    }

    private static String userEvent(UUID eventId, UUID userId, long revision, boolean enabled) {
        return """
                {"eventId":"%s","eventType":"UserAvailabilityChanged",\
                "streamKey":"USER:%s","revision":%d,"occurredAt":"%s",\
                "userId":"%s","enabled":%s}
                """.formatted(eventId, userId, revision, OffsetDateTime.now(), userId, enabled);
    }

    private static String grantEvent(
            UUID eventId,
            UUID userId,
            UUID domainId,
            long revision,
            boolean granted
    ) {
        return """
                {"eventId":"%s","eventType":"UserDomainGrantChanged",\
                "streamKey":"USER_DOMAIN:%s:%s","revision":%d,"occurredAt":"%s",\
                "userId":"%s","domainId":"%s","granted":%s}
                """.formatted(
                eventId,
                userId,
                domainId,
                revision,
                OffsetDateTime.now(),
                userId,
                domainId,
                granted
        );
    }

    private static ConsumerRecord<String, String> consumeOne(String topic, Duration timeout) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "dlt-integration-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        long deadline = System.nanoTime() + timeout.toNanos();
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(Set.of(topic));
            while (System.nanoTime() < deadline) {
                var records = consumer.poll(Duration.ofMillis(500));
                if (!records.isEmpty()) {
                    return records.iterator().next();
                }
            }
        }
        throw new AssertionError("未在期限内收到 DLT 消息: " + topic);
    }

    private static void await(Duration timeout, BooleanSupplier condition) {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new AssertionError("等待事件消费时被中断", exception);
            }
        }
        assertTrue(condition.getAsBoolean(), "未在期限内观察到期望的消费结果");
    }
}
