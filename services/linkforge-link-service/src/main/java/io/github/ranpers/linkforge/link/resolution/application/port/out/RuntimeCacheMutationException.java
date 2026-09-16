package io.github.ranpers.linkforge.link.resolution.application.port.out;

/**
 * 表示无法建立保证运行时缓存一致性所必需的写屏障。
 */
public final class RuntimeCacheMutationException extends RuntimeException {

    public RuntimeCacheMutationException(String message, Throwable cause) {
        super(message, cause);
    }
}
