package io.github.ranpers.linkforge.link.creation.application.port.in;

import io.github.ranpers.linkforge.link.creation.domain.InvalidShortLinkException;
import io.github.ranpers.linkforge.link.creation.domain.ShortCode;
import io.github.ranpers.linkforge.link.creation.domain.ShortCodeType;

import java.util.Objects;

/**
 * 明确区分系统自动分配与调用者指定短码，避免用可空字符串表达业务分支。
 */
public sealed interface ShortCodeRequest
        permits ShortCodeRequest.Auto, ShortCodeRequest.Custom {

    /**
     * 请求系统自动分配 10 位 Base62 短码。
     */
    record Auto() implements ShortCodeRequest {
    }

    /**
     * 请求使用已经通过领域校验的自定义短码。
     *
     * @param shortCode 类型必须为 {@link ShortCodeType#CUSTOM} 的非空短码
     */
    record Custom(ShortCode shortCode) implements ShortCodeRequest {
        public Custom {
            Objects.requireNonNull(shortCode, "shortCode");
            if (shortCode.type() != ShortCodeType.CUSTOM) {
                throw new IllegalArgumentException("自定义请求必须携带 CUSTOM 类型短码");
            }
        }
    }

    /**
     * 创建自动分配请求。
     *
     * @return 不携带候选值的自动分配请求
     */
    static ShortCodeRequest auto() {
        return new Auto();
    }

    /**
     * 创建自定义短码请求。
     *
     * @param value 4 至 32 位的自定义短码原始值，不允许为空
     * @return 已完成格式与保留词校验的自定义请求
     * @throws InvalidShortLinkException 输入格式非法或命中不区分大小写的系统保留词时
     */
    static ShortCodeRequest custom(String value) {
        return new Custom(ShortCode.custom(value));
    }
}
