package io.github.ranpers.linkforge.link.group.application;

/**
 * 目标分组不存在。
 *
 * <p>分组不属于当前操作者时同样抛出本异常，以免通过状态码区分「不存在」与「他人所有」。</p>
 */
public class GroupNotFoundException extends RuntimeException {

    public GroupNotFoundException() {
        super("分组不存在");
    }
}
