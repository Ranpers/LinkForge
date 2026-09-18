package io.github.ranpers.linkforge.iam.shortdomain.adapter.in.web;

import io.github.ranpers.linkforge.iam.shortdomain.domain.ShortDomainHost;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param host 主机名，可含大写与首尾空白，服务端会归一化为小写
 * @param name   展示名称，可为空
 */
public record CreateShortDomainRequest(
        @NotBlank @Size(max = ShortDomainHost.MAX_LENGTH) String host,
        @Size(max = 128) String name
) {
}
