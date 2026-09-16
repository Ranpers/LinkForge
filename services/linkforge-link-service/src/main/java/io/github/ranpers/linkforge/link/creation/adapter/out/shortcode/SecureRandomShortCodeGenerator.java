package io.github.ranpers.linkforge.link.creation.adapter.out.shortcode;

import io.github.ranpers.linkforge.link.creation.application.port.out.ShortCodeGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * 生成大小写敏感且不可预测的 10 位 Base62 候选短码。
 *
 * @implNote 复用进程级 {@link SecureRandom}，避免使用可能阻塞的
 * {@link SecureRandom#getInstanceStrong()}；候选值的数据库唯一性由创建流程负责。
 */
@Component
public class SecureRandomShortCodeGenerator implements ShortCodeGenerator {

    private static final char[] ALPHABET =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final int CODE_LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    @Override
    public String nextCode() {
        char[] value = new char[CODE_LENGTH];
        for (int index = 0; index < value.length; index++) {
            value[index] = ALPHABET[random.nextInt(ALPHABET.length)];
        }
        return new String(value);
    }
}
