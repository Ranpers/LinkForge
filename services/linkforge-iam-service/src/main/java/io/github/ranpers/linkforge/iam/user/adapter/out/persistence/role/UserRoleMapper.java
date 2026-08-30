package io.github.ranpers.linkforge.iam.user.adapter.out.persistence.role;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

@Mapper
public interface UserRoleMapper {

    /**
     * 叶子侧共享锁:多个用户可并发分配同一角色,但会与角色授权扇出的独占锁冲突。
     *
     * @return 已锁定角色 id;角色不存在时返回 null
     */
    UUID lockRoleSharedByCode(@Param("roleCode") String roleCode);

    /**
     * 扇出侧独占锁:未来 t_role_domain / t_role_domain_group 变更必须先调用本方法,
     * 防止与用户角色分配并发时双方受影响集合都为空。
     */
    UUID lockRoleExclusiveByCode(@Param("roleCode") String roleCode);

    int insertByRoleCode(
            @Param("userId") UUID userId,
            @Param("roleCode") String roleCode
    );

    /** 该角色当前授权的全部域名(直绑 ∨ 经域名组)直接暂存为批量影响集合。 */
    int stageGrantedDomainsForAssignment(
            @Param("userId") UUID userId,
            @Param("roleCode") String roleCode
    );
}
