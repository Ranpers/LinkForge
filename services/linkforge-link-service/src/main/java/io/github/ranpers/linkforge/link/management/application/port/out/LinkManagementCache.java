package io.github.ranpers.linkforge.link.management.application.port.out;

import java.util.UUID;

/**
 * 使管理写入后的短链运行时缓存与数据库提交结果一致。
 */
public interface LinkManagementCache {

    /**
     * 注册事务提交后的缓存刷新；事务回滚时不得暴露未提交状态。
     *
     * @param linkId 已发生管理写入的短链
     */
    void refreshAfterCommit(UUID linkId);
}
