package io.github.ranpers.linkforge.iam.grant.application;

/**
 * 投影行的加锁时刻快照。granted=false 且 revision=0 表示尚无任何授权翻转的初始流。
 */
public record GrantSnapshot(boolean granted, long revision) {
}
