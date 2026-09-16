package io.github.ranpers.linkforge.iam.authorization.application.port.in;

import io.github.ranpers.linkforge.iam.authorization.domain.SigningKey;

/**
 * 提供授权服务器当前可用于签发令牌的密钥。
 */
public interface LoadSigningKeyUseCase {

    /**
     * 返回当前活动密钥；不存在时生成并持久化一把新密钥。
     *
     * <p>实现必须保证并发初始化不会产生多把活动密钥。</p>
     *
     * @return 可立即用于签名的活动密钥
     */
    SigningKey loadOrCreate();
}
