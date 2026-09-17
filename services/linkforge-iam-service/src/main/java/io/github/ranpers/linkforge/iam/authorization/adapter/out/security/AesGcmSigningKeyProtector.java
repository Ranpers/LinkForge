package io.github.ranpers.linkforge.iam.authorization.adapter.out.security;

import io.github.ranpers.linkforge.iam.authorization.application.port.out.SigningKeyProtector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 使用 AES-256-GCM 对数据库中的 PKCS#8 私钥进行认证加密。
 *
 * @implNote 每次写入生成 96 位随机 IV；密钥标识作为附加认证数据，防止密文被换绑。
 */
@Component
public final class AesGcmSigningKeyProtector implements SigningKeyProtector {

    private static final String FORMAT_PREFIX = "enc:v1:";
    private static final int AES_256_KEY_BYTES = 32;
    private static final int IV_BYTES = 12;
    private static final int GCM_TAG_BITS = 128;

    private final SecretKeySpec encryptionKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public AesGcmSigningKeyProtector(
            @Value("${linkforge.security.signing-key-encryption-key}") String encodedKey
    ) {
        byte[] keyBytes = decodeKey(encodedKey);
        this.encryptionKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public String protect(String keyId, String privateKeyDer) {
        byte[] iv = new byte[IV_BYTES];
        secureRandom.nextBytes(iv);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            cipher.updateAAD(keyId.getBytes(StandardCharsets.UTF_8));
            byte[] ciphertext = cipher.doFinal(privateKeyDer.getBytes(StandardCharsets.UTF_8));
            byte[] payload = ByteBuffer.allocate(iv.length + ciphertext.length)
                    .put(iv)
                    .put(ciphertext)
                    .array();
            return FORMAT_PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("签名私钥加密失败", exception);
        }
    }

    @Override
    public String unprotect(String keyId, String protectedPrivateKey) {
        if (protectedPrivateKey == null || !protectedPrivateKey.startsWith(FORMAT_PREFIX)) {
            throw new IllegalStateException("签名私钥不是受支持的 enc:v1 密文");
        }
        try {
            byte[] payload = Base64.getDecoder().decode(
                    protectedPrivateKey.substring(FORMAT_PREFIX.length())
            );
            if (payload.length <= IV_BYTES) {
                throw new IllegalStateException("签名私钥密文格式错误");
            }
            byte[] iv = new byte[IV_BYTES];
            byte[] ciphertext = new byte[payload.length - IV_BYTES];
            System.arraycopy(payload, 0, iv, 0, IV_BYTES);
            System.arraycopy(payload, IV_BYTES, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, encryptionKey, new GCMParameterSpec(GCM_TAG_BITS, iv));
            cipher.updateAAD(keyId.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
        } catch (AEADBadTagException exception) {
            throw new IllegalStateException("签名私钥密文认证失败", exception);
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new IllegalStateException("签名私钥解密失败", exception);
        }
    }

    private static byte[] decodeKey(String encodedKey) {
        try {
            byte[] key = Base64.getDecoder().decode(encodedKey.trim());
            if (key.length != AES_256_KEY_BYTES) {
                throw new IllegalArgumentException("必须解码为 32 字节");
            }
            return key;
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "IAM_SIGNING_KEY_ENCRYPTION_KEY 必须是 32 字节密钥的 Base64 编码",
                    exception
            );
        }
    }
}
