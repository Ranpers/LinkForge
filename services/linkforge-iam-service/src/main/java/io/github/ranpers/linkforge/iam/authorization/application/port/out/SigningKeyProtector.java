package io.github.ranpers.linkforge.iam.authorization.application.port.out;

/**
 * 保护持久化的签名私钥材料。
 */
public interface SigningKeyProtector {

    /**
     * 使用密钥标识作为附加认证数据加密私钥。
     *
     * @param keyId         与私钥绑定的非空密钥标识
     * @param privateKeyDer Base64 编码的非空 PKCS#8 私钥
     * @return 带格式版本的密文
     */
    String protect(String keyId, String privateKeyDer);

    /**
     * 解密受保护私钥。
     *
     * @param keyId               与私钥绑定的非空密钥标识
     * @param protectedPrivateKey 数据库存储的非空私钥材料
     * @return Base64 编码的 PKCS#8 私钥
     * @throws IllegalStateException 值不是受支持的密文、认证失败或无法解密时
     */
    String unprotect(String keyId, String protectedPrivateKey);
}
