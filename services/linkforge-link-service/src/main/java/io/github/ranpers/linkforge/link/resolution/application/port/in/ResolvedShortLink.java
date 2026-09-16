package io.github.ranpers.linkforge.link.resolution.application.port.in;

/**
 * 通过全部运行时可用性检查后的短链解析结果。
 *
 * @param targetUrl 可用于 HTTP 重定向的绝对目标地址
 */
public record ResolvedShortLink(String targetUrl) {
}
