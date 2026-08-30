package io.github.ranpers.linkforge.iam.grant.application;

import io.github.ranpers.linkforge.iam.grant.application.port.out.OutboxDispatchStore;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(properties = {
        "linkforge.outbox.enabled=false",
        "spring.security.oauth2.authorizationserver.client.smoke-test.registration.client-secret={noop}integration-secret"
})
class OutboxDispatcherIntegrationTest {

    private static final String TOPIC = "linkforge.iam.authorization.v1";

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
        registry.add("linkforge.outbox.topic", () -> TOPIC);
    }

    @Autowired
    private OutboxDispatchService dispatchService;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private OutboxDispatchStore dispatchStore;

    @Autowired
    private TransactionTemplate transactions;

    @Test
    void shouldDispatchConcurrentBatchesWithoutDoubleClaim() throws Exception {
        UUID firstDomainId = UUID.randomUUID();
        UUID secondDomainId = UUID.randomUUID();
        UUID firstId = insertPendingEvent(UUID.randomUUID(), firstDomainId);
        UUID secondId = insertPendingEvent(UUID.randomUUID(), secondDomainId);
        var settings = new OutboxDispatchSettings(
                1,
                3,
                Duration.ofMillis(100),
                Duration.ofSeconds(1),
                Duration.ofSeconds(10)
        );
        CountDownLatch start = new CountDownLatch(1);

        OutboxDispatchSummary firstSummary;
        OutboxDispatchSummary secondSummary;
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<OutboxDispatchSummary> first = executor.submit(() -> {
                start.await();
                return dispatchService.dispatchBatch(settings);
            });
            Future<OutboxDispatchSummary> second = executor.submit(() -> {
                start.await();
                return dispatchService.dispatchBatch(settings);
            });
            start.countDown();
            firstSummary = first.get(20, TimeUnit.SECONDS);
            secondSummary = second.get(20, TimeUnit.SECONDS);
        }

        assertEquals(1, firstSummary.sent());
        assertEquals(1, secondSummary.sent());
        assertEquals(
                Map.of(firstId, firstDomainId.toString(), secondId, secondDomainId.toString()),
                consumeEventKeys(2, Duration.ofSeconds(15))
        );
        assertSent(firstId);
        assertSent(secondId);
    }

    @Test
    void shouldPersistRetryDiagnosticsAndParkedState() {
        UUID eventId = insertPendingEvent(UUID.randomUUID(), UUID.randomUUID());
        OffsetDateTime beforeRetry = OffsetDateTime.now();

        transactions.executeWithoutResult(ignored -> {
            var events = dispatchStore.lockDueRows(1);
            assertEquals(1, events.size());
            assertEquals(eventId, events.getFirst().id());
            dispatchStore.scheduleRetry(eventId, 1, Duration.ofSeconds(3), "broker unavailable");
        });

        Map<String, Object> retryRow = findState(eventId);
        assertEquals(0, ((Number) retryRow.get("status")).intValue());
        assertEquals(1, ((Number) retryRow.get("retry_count")).intValue());
        assertTrue(((Timestamp) retryRow.get("next_retry_at")).toInstant().isAfter(beforeRetry.toInstant()));
        assertNotNull(retryRow.get("last_attempt_at"));
        assertEquals("broker unavailable", retryRow.get("last_error"));

        transactions.executeWithoutResult(ignored ->
                dispatchStore.park(eventId, 2, "retry limit reached")
        );

        Map<String, Object> parkedRow = findState(eventId);
        assertEquals(2, ((Number) parkedRow.get("status")).intValue());
        assertEquals(2, ((Number) parkedRow.get("retry_count")).intValue());
        assertNull(parkedRow.get("next_retry_at"));
        assertNotNull(parkedRow.get("last_attempt_at"));
        assertEquals("retry limit reached", parkedRow.get("last_error"));
    }

    private UUID insertPendingEvent(UUID userId, UUID domainId) {
        UUID eventId = UUID.randomUUID();
        String streamKey = "USER_DOMAIN:" + userId + ":" + domainId;
        String payload = """
                {"eventId":"%s","eventType":"UserDomainGrantChanged","streamKey":"%s",\
                "revision":1,"occurredAt":"%s","userId":"%s","domainId":"%s","granted":false}
                """.formatted(eventId, streamKey, OffsetDateTime.now(), userId, domainId);
        jdbc.update(
                """
                INSERT INTO t_outbox_event (id, event_type, stream_key, partition_key, payload)
                VALUES (?, 'UserDomainGrantChanged', ?, ?, CAST(? AS jsonb))
                """,
                eventId,
                streamKey,
                domainId.toString(),
                payload
        );
        return eventId;
    }

    private Map<UUID, String> consumeEventKeys(int expected, Duration timeout) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "outbox-integration-" + UUID.randomUUID());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        Map<UUID, String> eventKeys = new HashMap<>();
        long deadline = System.nanoTime() + timeout.toNanos();
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(Set.of(TOPIC));
            while (eventKeys.size() < expected && System.nanoTime() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                    assertNotNull(record.key());
                    eventKeys.put(extractEventId(record.value()), record.key());
                }
            }
        }
        assertEquals(expected, eventKeys.size(), "未在期限内收到预期 Kafka 事件数");
        return eventKeys;
    }

    private static UUID extractEventId(String payload) {
        String marker = "\"eventId\": \"";
        int start = payload.indexOf(marker);
        assertTrue(start >= 0, "Kafka 消息缺少 eventId: " + payload);
        start += marker.length();
        int end = payload.indexOf('"', start);
        return UUID.fromString(payload.substring(start, end));
    }

    private void assertSent(UUID eventId) {
        Map<String, Object> row = findState(eventId);
        assertEquals(1, ((Number) row.get("status")).intValue());
        assertEquals(0, ((Number) row.get("retry_count")).intValue());
        assertNotNull(row.get("sent_at"));
        assertNotNull(row.get("last_attempt_at"));
        assertNull(row.get("last_error"));
    }

    private Map<String, Object> findState(UUID eventId) {
        return jdbc.queryForMap(
                """
                SELECT status, retry_count, next_retry_at, sent_at, last_attempt_at, last_error
                FROM t_outbox_event
                WHERE id = ?
                """,
                eventId
        );
    }
}
