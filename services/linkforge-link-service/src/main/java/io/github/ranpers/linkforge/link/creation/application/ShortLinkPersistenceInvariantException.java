package io.github.ranpers.linkforge.link.creation.application;

public class ShortLinkPersistenceInvariantException extends RuntimeException {
    public ShortLinkPersistenceInvariantException() {
        super("短链插入未成功且无法识别唯一约束冲突");
    }
}
