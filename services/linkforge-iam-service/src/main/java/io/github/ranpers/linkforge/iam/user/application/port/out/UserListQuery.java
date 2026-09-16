package io.github.ranpers.linkforge.iam.user.application.port.out;

import io.github.ranpers.linkforge.iam.user.application.port.in.UserListItem;

import java.util.List;

/** 用户列表只读持久化端口。 */
public interface UserListQuery {
    List<UserListItem> find(UserListCriteria criteria);
}
