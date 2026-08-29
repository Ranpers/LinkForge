-- =============================================================
-- V4__seed_prototype_domain.sql — 原型期种子:业务域名 + USER 角色授权
--
-- 设计约定:
--   1) 原型期尚无域名管理用例,以迁移种子让"注册即拥有 USER 角色种子的授权"成立,
--      link 创建校验与授权投影管道由此具备真实数据;域名管理用例落地后由管理端接管
--   2) 迁移属初始化,不经投影管道;绑定写入后必须同步维护投影(下方回填),
--      无存量用户时回填为 no-op
-- =============================================================

INSERT INTO t_domain (domain, name)
VALUES ('go.linkforge.dev', '原型演示域名');

INSERT INTO t_role_domain (role_id, domain_id)
SELECT role.id, domain.id
FROM t_role role
JOIN t_domain domain ON domain.domain = 'go.linkforge.dev'
WHERE role.code = 'USER';

-- 投影回填:存量用户按四路并集补齐种子域名的授权流(与 V3 回填同口径,限定种子域名)
INSERT INTO t_user_domain_grant_state (user_id, domain_id, granted, revision)
SELECT user_id, domain_id, TRUE, 1
FROM (
         SELECT ur.user_id, rd.domain_id
         FROM t_user_role ur
                  JOIN t_role_domain rd ON rd.role_id = ur.role_id
         UNION
         SELECT ur.user_id, dgd.domain_id
         FROM t_user_role ur
                  JOIN t_role_domain_group rdg ON rdg.role_id = ur.role_id
                  JOIN t_domain_group_domain dgd ON dgd.domain_group_id = rdg.domain_group_id
     ) grant_pairs
WHERE NOT EXISTS (
    SELECT 1
    FROM t_user_domain_grant_state existing
    WHERE existing.user_id = grant_pairs.user_id
      AND existing.domain_id = grant_pairs.domain_id
);
