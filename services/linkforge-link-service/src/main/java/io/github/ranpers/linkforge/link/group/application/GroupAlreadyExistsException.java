package io.github.ranpers.linkforge.link.group.application;

/** 同一用户的活跃分组中出现重名。已软删除的分组不参与判重，因此不影响重建同名分组。 */
public class GroupAlreadyExistsException extends RuntimeException {

    public GroupAlreadyExistsException() {
        super("同名分组已存在");
    }
}
