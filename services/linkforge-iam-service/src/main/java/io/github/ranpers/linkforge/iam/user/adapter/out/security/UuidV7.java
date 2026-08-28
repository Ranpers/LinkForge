package io.github.ranpers.linkforge.iam.user.adapter.out.security;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * IAM 用户 ID 的 UUIDv7 生成实现。
 */
final class UuidV7 {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static long lastTimestamp = -1L;
    private static long lastRandA;

    private UuidV7() {
    }

    /**
     * 时钟停滞或回拨时沿用逻辑时间；单逻辑毫秒序列耗尽后推进 1ms，保证进程内严格单调且不回绕。
     */
    static synchronized UUID generate() {
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
