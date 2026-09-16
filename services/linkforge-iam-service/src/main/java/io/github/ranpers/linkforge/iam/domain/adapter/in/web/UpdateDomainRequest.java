package io.github.ranpers.linkforge.iam.domain.adapter.in.web;

import jakarta.validation.constraints.Size;

/**
 * 修改域名。主机名不可修改，请求体中不需要携带。
 *
 * @param name 新的展示名称；缺省或为 {@code null} 时清空该字段
 */
public record UpdateDomainRequest(@Size(max = 128) String name) {
}
