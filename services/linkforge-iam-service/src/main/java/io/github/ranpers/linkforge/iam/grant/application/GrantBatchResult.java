package io.github.ranpers.linkforge.iam.grant.application;

/** 一次授权关系变更的影响规模和最终权限翻转数。 */
public record GrantBatchResult(int affectedPairs, int flippedPairs) {

    public GrantBatchResult {
        if (flippedPairs < 0 || flippedPairs > affectedPairs) {
            throw new IllegalArgumentException("授权批次计数不合法");
        }
    }

    public static GrantBatchResult empty() {
        return new GrantBatchResult(0, 0);
    }
}
