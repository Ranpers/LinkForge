package io.github.ranpers.linkforge.iam.grant.application.port.in;

/**
 * IAM 可评估的短链管理动作及其对应权限码。
 */
public enum LinkManagementAction {
    /** 修改短链内容或可用状态。 */
    UPDATE("link:update"),
    /** 删除短链。 */
    DELETE("link:delete");

    private final String permissionCode;

    LinkManagementAction(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    /**
     * 返回评估该动作所需的权限码。
     *
     * @return IAM 权限表中使用的稳定权限码
     */
    public String permissionCode() {
        return permissionCode;
    }
}
