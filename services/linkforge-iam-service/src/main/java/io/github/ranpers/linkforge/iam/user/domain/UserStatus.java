package io.github.ranpers.linkforge.iam.user.domain;

@SuppressWarnings("unused")
public enum UserStatus {
    DEACTIVATED(0),
    ACTIVE(1),
    SECURITY_SUSPENDED(2);

    private final int databaseValue;

    UserStatus(int databaseValue) {
        this.databaseValue = databaseValue;
    }

    public int databaseValue() {
        return databaseValue;
    }

    public static UserStatus fromDatabaseValue(int value) {
        return switch (value) {
            case 0 -> DEACTIVATED;
            case 1 -> ACTIVE;
            case 2 -> SECURITY_SUSPENDED;
            default -> throw new IllegalArgumentException("未知用户状态: " + value);
        };
    }
}
