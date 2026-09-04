package io.github.ranpers.linkforge.link.resolution.application.port.in;

import io.github.ranpers.linkforge.link.resolution.application.ShortLinkUnavailableException;

/**
 * 根据请求域名和短码解析可跳转的目标地址。
 */
public interface ResolveShortLinkUseCase {

    /**
     * 解析短链并统一执行状态、删除、过期、域名和安全限制检查。
     *
     * @param host 不含协议和端口的请求主机名；匹配时忽略大小写和首尾空白
     * @param linkCode 域名内的短码
     * @return 仅在短链当前允许访问时返回的目标地址
     * @throws ShortLinkUnavailableException 短链不存在或任一可用性检查未通过时
     */
    ResolvedShortLink resolve(String host, String linkCode);
}
