package io.github.ranpers.linkforge.link.infrastructure.id;

import java.security.SecureRandom;
import java.util.UUID;

public final class UuidV7 {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static long lastTimestamp = -1L;
    private static long sequence;

    private UuidV7() {
    }

    public static synchronized UUID generate() {
        long now = System.currentTimeMillis();
        if (now > lastTimestamp) {
            lastTimestamp = now;
            sequence = 0;
        } else if (sequence < 0x0FFFL) {
            sequence++;
        } else {
            lastTimestamp++;
            sequence = 0;
        }
        long msb = (lastTimestamp & 0xFFFFFFFFFFFFL) << 16 | 0x7000L | sequence;
        long lsb = (RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL) | 0x8000000000000000L;
        return new UUID(msb, lsb);
    }
}
