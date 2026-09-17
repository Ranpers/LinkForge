package io.github.ranpers.linkforge.iam.authorization.application.port.out;

import io.github.ranpers.linkforge.iam.authorization.domain.SigningKey;

import java.util.Optional;

/**
 * 持久化授权服务器的活动签名密钥，并提供初始化所需的跨实例互斥。
 */
public interface SigningKeyStore {

    /**
     * 获取持续到当前事务结束的初始化锁。
     *
     * @apiNote 必须在数据库事务内调用，锁的具体机制由输出适配器决定
     */
    void lockForInitialization();

    /**
     * 读取当前活动密钥，并解密持久化的私钥材料。
     *
     * @return 当前活动密钥；尚未初始化时为空
     */
    Optional<SigningKey> findActive();

    /**
     * 持久化一把活动密钥，私钥材料必须在适配器边界内加密。
     *
     * @param signingKey 包含完整公私钥材料的新签名密钥
     */
    void save(SigningKey signingKey);
}
