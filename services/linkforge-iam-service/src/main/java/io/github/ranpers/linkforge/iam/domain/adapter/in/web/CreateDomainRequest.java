package io.github.ranpers.linkforge.iam.domain.adapter.in.web;

import io.github.ranpers.linkforge.iam.domain.domain.DomainHost;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * @param domain 主机名，可含大写与首尾空白，服务端会归一化为小写
 * @param name   展示名称，可为空
 */
public record CreateDomainRequest(
        @NotBlank @Size(max = DomainHost.MAX_LENGTH) String domain,
        @Size(max = 128) String name
) {
}
