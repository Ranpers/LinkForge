package io.github.ranpers.linkforge.iam.user.domain;

@SuppressWarnings("unused")
public enum UserStatus {
    DISABLED(0),
    ACTIVE(1);

    private final int databaseValue;

    UserStatus(int databaseValue) {
        this.databaseValue = databaseValue;
    }

    public int databaseValue() {
        return databaseValue;
    }

    public static UserStatus fromDatabaseValue(int value) {
        return switch (value) {
            case 0 -> DISABLED;
            case 1 -> ACTIVE;
            default -> throw new IllegalArgumentException("未知用户状态: " + value);
        };
    }
}
