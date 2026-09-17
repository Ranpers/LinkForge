package io.github.ranpers.linkforge.webmvc.pagination;

import java.util.List;

/**
 * 基于稳定游标的列表响应。
 *
 * @param items 当前页数据
 * @param nextCursor 下一页游标；没有下一页时为 null
 * @param hasMore 是否还有下一页
 */
public record CursorPageResponse<T>(List<T> items, String nextCursor, boolean hasMore) {
    public CursorPageResponse {
        items = List.copyOf(items);
    }
}
