package io.github.ranpers.linkforge.iam.user.application.port.in;

/** 用户列表支持的账号状态。 */
public enum UserListStatus {
    DEACTIVATED(0),
    ACTIVE(1),
    SECURITY_SUSPENDED(2);

    private final int databaseValue;

    UserListStatus(int databaseValue) {
        this.databaseValue = databaseValue;
    }

    public int databaseValue() {
        return databaseValue;
    }
}
