package io.github.ranpers.linkforge.iam.grant.application;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OutboxDispatchSettingsTest {

    @Test
    void shouldCalculateCappedExponentialBackoff() {
        var settings = new OutboxDispatchSettings(
                50,
                8,
                Duration.ofSeconds(1),
                Duration.ofSeconds(5),
                Duration.ofSeconds(10)
        );

        assertEquals(Duration.ofSeconds(1), settings.retryDelay(1));
        assertEquals(Duration.ofSeconds(2), settings.retryDelay(2));
        assertEquals(Duration.ofSeconds(4), settings.retryDelay(3));
        assertEquals(Duration.ofSeconds(5), settings.retryDelay(4));
        assertEquals(Duration.ofSeconds(5), settings.retryDelay(100));
    }

    @Test
    void shouldRejectInvalidSettings() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new OutboxDispatchSettings(
                        0,
                        8,
                        Duration.ofSeconds(1),
                        Duration.ofMinutes(1),
                        Duration.ofSeconds(10)
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new OutboxDispatchSettings(
                        1,
                        1,
                        Duration.ofSeconds(2),
                        Duration.ofSeconds(1),
                        Duration.ofSeconds(10)
                )
        );
    }
}
