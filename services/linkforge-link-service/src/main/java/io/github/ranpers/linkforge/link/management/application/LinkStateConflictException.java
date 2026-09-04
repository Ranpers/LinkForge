package io.github.ranpers.linkforge.link.management.application;

public class LinkStateConflictException extends RuntimeException {
    public LinkStateConflictException() {
        super("该短链不是普通手动停用状态，不能由此接口恢复");
    }
}
