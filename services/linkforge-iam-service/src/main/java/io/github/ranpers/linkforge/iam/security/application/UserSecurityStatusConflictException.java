package io.github.ranpers.linkforge.iam.security.application;

public class UserSecurityStatusConflictException extends RuntimeException {
    public UserSecurityStatusConflictException() {
        super("停用或删除用户不能通过安全解冻接口变更为活动状态");
    }
}
