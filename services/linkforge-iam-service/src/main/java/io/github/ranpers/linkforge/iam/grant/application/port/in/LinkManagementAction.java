package io.github.ranpers.linkforge.iam.grant.application.port.in;

public enum LinkManagementAction {
    UPDATE("link:update"),
    DELETE("link:delete");

    private final String permissionCode;

    LinkManagementAction(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String permissionCode() {
        return permissionCode;
    }
}
