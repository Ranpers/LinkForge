package io.github.ranpers.linkforge.iam.shortdomain.adapter.out.persistence;

import io.github.ranpers.linkforge.iam.shortdomain.application.port.in.ShortDomainListItem;

import java.time.Instant;
import java.util.UUID;

/**
 * 域名创建语句的单行返回。
 *
 * <p>{@code create} 无论成功、主机名冲突还是权限拒绝都只返回一行，未产生插入时除
 * {@code resultCode} 外全部为 {@code null}，因此字段一律使用包装类型而非基本类型。</p>
 *
 * @param resultCode 结果码，见 {@link MybatisShortDomainStore}
 * @param id         新建域名的标识；未创建时为 {@code null}
 * @param host     归一化主机名；未创建时为 {@code null}
 * @param name       展示名称；未创建或未填写时为 {@code null}
 * @param enabled    是否启用；未创建时为 {@code null}
 * @param revision   控制事件版本号；未创建时为 {@code null}
 * @param createdAt  创建时间；未创建时为 {@code null}
 * @param updatedAt  更新时间；未创建时为 {@code null}
 */
public record ShortDomainCreationRow(
        Integer resultCode,
        UUID id,
        String host,
        String name,
        Boolean enabled,
        Long revision,
        Instant createdAt,
        Instant updatedAt
) {

    /** 仅在结果码表示创建成功时调用。 */
    ShortDomainListItem toItem() {
        return new ShortDomainListItem(
                id,
                host,
                name,
                Boolean.TRUE.equals(enabled),
                revision == null ? 1L : revision,
                createdAt,
                updatedAt
        );
    }
}
