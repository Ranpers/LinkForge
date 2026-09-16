package io.github.ranpers.linkforge.iam.infrastructure.id;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * 进程内唯一 UUIDv7 生成器:单逻辑毫秒内序列耗尽则推进 1ms,严格单调且不回绕。
 * 用户 ID 与 Outbox eventId 共用同一生成器,保证全进程时钟语义一致。
 */
public final class UuidV7 {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static long lastTimestamp = -1L;
    private static long lastRandA;

    private UuidV7() {
    }

    public static synchronized UUID generate() {
        long wallClockTimestamp = System.currentTimeMillis();
        if (wallClockTimestamp > lastTimestamp) {
            lastTimestamp = wallClockTimestamp;
            lastRandA = 0L;
        } else if (lastRandA < 0x0FFFL) {
            lastRandA++;
        } else {
            lastTimestamp++;
            lastRandA = 0L;
        }

        long randB = SECURE_RANDOM.nextLong() & 0x3FFFFFFFFFFFFFFFL;

        long msb = (lastTimestamp & 0xFFFFFFFFFFFFL) << 16
                | 0x7000L
                | lastRandA;
        long lsb = randB | 0x8000000000000000L;

        return new UUID(msb, lsb);
    }
}
