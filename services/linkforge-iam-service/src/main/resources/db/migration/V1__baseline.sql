-- =============================================================
-- V1__baseline.sql — IAM 基线 schema
--
-- 设计约定:
--   1) 主键统一 uuid7(PG 18 内置),即「内部关联键 = 对外 public id」,
--      不设双 id(自增内部 id + 外露 uuid 两套)
--   2) 关联表用复合主键(两外键),不设代理键
--   3) 权限码 = 资源:动作(link:create);OAuth2 scope 用 . 分隔(link.write),两者分层
--   4) 软删除统一 deleted_at(NULL=存活);禁用/失效另用 status 或 disabled_reason 表达
-- =============================================================

-- ---------- 用户 ----------

CREATE TABLE t_user
(
    id         uuid PRIMARY KEY      DEFAULT uuidv7(),
    username   varchar(64)  NOT NULL UNIQUE,
    password   varchar(255) NOT NULL,           -- 密码哈希(Argon2),禁明文
    email      varchar(128),
    real_name  varchar(64),
    status     smallint     NOT NULL DEFAULT 1, -- 1=正常 0=禁用
    deleted_at timestamptz,                     -- 软删:NULL=存活,非NULL=已删号
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now()
);

-- ---------- RBAC ----------

CREATE TABLE t_role
(
    id          uuid PRIMARY KEY     DEFAULT uuidv7(),
    code        varchar(32) NOT NULL UNIQUE, -- USER / ADMIN
    name        varchar(64) NOT NULL,
    description varchar(255),
    created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE t_permission
(
    id          uuid PRIMARY KEY     DEFAULT uuidv7(),
    code        varchar(64) NOT NULL UNIQUE, -- link:create / group:delete ...
    name        varchar(64) NOT NULL,
    description varchar(255),
    created_at  timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE t_user_role
(
    user_id    uuid        NOT NULL REFERENCES t_user (id) ON DELETE CASCADE,
    role_id    uuid        NOT NULL REFERENCES t_role (id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);
CREATE INDEX idx_user_role_role ON t_user_role (role_id); -- 反查「某角色有哪些用户」

CREATE TABLE t_role_permission
(
    role_id       uuid        NOT NULL REFERENCES t_role (id) ON DELETE CASCADE,
    permission_id uuid        NOT NULL REFERENCES t_permission (id) ON DELETE CASCADE,
    created_at    timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (role_id, permission_id)
);
CREATE INDEX idx_role_perm_perm ON t_role_permission (permission_id);

-- ---------- 域名授权 ----------
-- 「域名白名单」判断在应用层:host == domain || host.endsWith('.' + domain)
-- (子域命中、点号做边界,天然排除 badexample.com)
-- 「谁能在这个域名下创建短链」由下面绑定动态求并集(角色/域名组/用户 4 条路径),
-- 与 t_domain.status(启用/禁用)是两回事:status 是全局开关,绑定是"授权给谁"。

CREATE TABLE t_domain
(
    id         uuid PRIMARY KEY      DEFAULT uuidv7(),
    domain     varchar(253) NOT NULL UNIQUE,    -- 归一化:小写、去协议/端口/路径,只存 host
    name       varchar(128),
    status     smallint     NOT NULL DEFAULT 1, -- 1=启用 0=禁用(禁用→其下所有短链失效 + 禁止新增)
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE t_domain_group
(
    id         uuid PRIMARY KEY      DEFAULT uuidv7(),
    code       varchar(64)  NOT NULL UNIQUE, -- 如 marketing-domains
    name       varchar(128) NOT NULL,
    created_at timestamptz  NOT NULL DEFAULT now(),
    updated_at timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE t_domain_group_domain
(
    domain_group_id uuid        NOT NULL REFERENCES t_domain_group (id) ON DELETE CASCADE,
    domain_id       uuid        NOT NULL REFERENCES t_domain (id) ON DELETE CASCADE,
    created_at      timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (domain_group_id, domain_id)
);
CREATE INDEX idx_dgd_domain ON t_domain_group_domain (domain_id);

CREATE TABLE t_role_domain
(
    role_id    uuid        NOT NULL REFERENCES t_role (id) ON DELETE CASCADE,
    domain_id  uuid        NOT NULL REFERENCES t_domain (id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (role_id, domain_id)
);
CREATE INDEX idx_role_domain_domain ON t_role_domain (domain_id);

CREATE TABLE t_role_domain_group
(
    role_id         uuid        NOT NULL REFERENCES t_role (id) ON DELETE CASCADE,
    domain_group_id uuid        NOT NULL REFERENCES t_domain_group (id) ON DELETE CASCADE,
    created_at      timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (role_id, domain_group_id)
);

CREATE TABLE t_user_domain
(
    user_id    uuid        NOT NULL REFERENCES t_user (id) ON DELETE CASCADE,
    domain_id  uuid        NOT NULL REFERENCES t_domain (id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, domain_id)
);

CREATE TABLE t_user_domain_group
(
    user_id         uuid        NOT NULL REFERENCES t_user (id) ON DELETE CASCADE,
    domain_group_id uuid        NOT NULL REFERENCES t_domain_group (id) ON DELETE CASCADE,
    created_at      timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, domain_group_id)
);

-- ---------- 种子数据 ----------

INSERT INTO t_role (code, name, description)
VALUES ('USER', '普通用户', '默认角色:管理自己的分组与短链'),
       ('ADMIN', '平台管理员', '管理全部租户、分组与短链');

INSERT INTO t_permission (code, name, description)
VALUES
    -- 用户域
    ('user:read', '查看个人信息', '查看自己的资料'),
    ('user:update', '维护个人信息', '修改头像、邮箱等资料'),
    -- 分组域
    ('group:create', '创建分组', '新建分组'),
    ('group:read', '查看分组', '查询分组列表'),
    ('group:update', '编辑分组', '修改分组名/排序'),
    ('group:delete', '删除分组', '删除分组'),
    -- 短链域
    ('link:create', '创建短链', '单个/批量创建短链'),
    ('link:read', '查看短链', '分页检索短链'),
    ('link:update', '编辑短链', '修改/设置有效期'),
    ('link:delete', '删除短链', '回收站删除/恢复/彻底清除'),
    -- 统计域
    ('stats:read', '查看统计', '单链/分组报表查询'),
    -- 平台管理
    ('user:manage', '用户管理', '管理全部用户(平台管理员)');

-- USER:自助服务权限(不含 user:manage)
INSERT INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r,
     t_permission p
WHERE r.code = 'USER'
  AND p.code IN ('user:read', 'user:update',
                 'group:create', 'group:read', 'group:update', 'group:delete',
                 'link:create', 'link:read', 'link:update', 'link:delete',
                 'stats:read');

-- ADMIN:全部权限
INSERT INTO t_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM t_role r
         CROSS JOIN t_permission p
WHERE r.code = 'ADMIN';
