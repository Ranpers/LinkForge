package io.github.ranpers.linkforge.iam.shortdomain.domain;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 短链域名主机名。
 *
 * <p>构造时去除首尾空白并转换为小写，归一化结果必须是不含协议、端口、路径与结尾点的点分主机名。
 * 归一化后的值同时满足 {@code t_short_domain} 的 {@code host} 列语义与 link-service
 * {@code t_short_domain_state.host} 的 {@code CHECK (host = lower(host) AND host !~ '[/:]')} 约束，
 * 因此以本类型构造的值可以安全写入跨服务控制事件。</p>
 *
 * @param value 归一化后的主机名，保证为小写、至少两个标签、总长不超过 253
 */
public record ShortDomainHost(String value) {

    /** 与 {@code t_short_domain.host} 以及 {@code t_short_domain_state.host} 的列宽一致。 */
    public static final int MAX_LENGTH = 253;

    /**
     * 至少两个以点分隔的标签，每个标签为 1 至 63 个字符，仅含小写字母、数字与内部连字符。
     *
     * <p>要求至少两个标签是为了排除 {@code localhost} 之类的单标签名称：跳转域名必须能被公网解析。</p>
     */
    private static final Pattern FORMAT = Pattern.compile(
            "^(?=.{1," + MAX_LENGTH + "}$)"
                    + "[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?"
                    + "(?:\\.[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?)+$"
    );

    public ShortDomainHost {
        if (value == null || value.isBlank()) {
            throw new InvalidShortDomainHostException("域名不能为空");
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > MAX_LENGTH) {
            throw new InvalidShortDomainHostException("域名长度不能超过 " + MAX_LENGTH);
        }
        if (!FORMAT.matcher(normalized).matches()) {
            throw new InvalidShortDomainHostException(
                    "域名必须是不含协议、端口、路径与结尾点的小写点分主机名"
            );
        }
        value = normalized;
    }
}
