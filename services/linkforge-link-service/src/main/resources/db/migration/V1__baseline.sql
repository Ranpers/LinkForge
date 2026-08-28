-- =============================================================
-- V1__baseline.sql — 短链服务基线 schema
--
-- 设计约定:
--   1) 主键统一 uuid7(PG 18 内置),即「内部关联键 = 对外 public id」
--   2) 跨服务引用(user_id→IAM t_user、domain_id→IAM t_domain)一律存裸 uuid,不设外键;
--      完整性靠服务边界 + 事件保证,不靠数据库约束
--   3) 同库内引用(group_id→t_group)保留真实外键
--   4) 软删除统一 deleted_at(NULL=存活);禁用/失效用 disabled_reason 位掩码
-- =============================================================

-- 个人分类文件夹(归属人引用 IAM t_user,裸 uuid)
CREATE TABLE t_group
(
    id         uuid PRIMARY KEY     DEFAULT uuidv7(),
    user_id    uuid        NOT NULL,          -- 裸 uuid:归属人(→IAM t_user.id)
    name       varchar(64) NOT NULL,
    sort_order int         NOT NULL DEFAULT 0,
    deleted_at timestamptz,                    -- 软删
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_group_user ON t_group (user_id);

CREATE TABLE t_link
(
    id              uuid PRIMARY KEY      DEFAULT uuidv7(),
    user_id         uuid        NOT NULL,          -- 裸 uuid:归属人(→IAM t_user.id)
    group_id        uuid        REFERENCES t_group (id) ON DELETE SET NULL, -- 所属文件夹,NULL=未分组
    name            varchar(64) NOT NULL,
    link_code       varchar(64) NOT NULL UNIQUE,    -- 短码(唯一,跳转主键)
    full_url        varchar(255) NOT NULL,          -- 长链(可重复,不唯一)
    sort_order      int         NOT NULL DEFAULT 0,
    domain_id       uuid        NOT NULL,          -- 裸 uuid:所在域名(→IAM t_domain.id)
    disabled_reason smallint    NOT NULL DEFAULT 0, -- 位掩码:0=正常 1=手动禁用 2=域名禁用 4=失去域权限
    deleted_at      timestamptz,                    -- 软删/回收站
    created_at      timestamptz NOT NULL DEFAULT now(),
    updated_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_link_user ON t_link (user_id);     -- 事件驱动批量失效:按归属人定位
CREATE INDEX idx_link_group ON t_link (group_id);   -- 文件夹内列表
CREATE INDEX idx_link_domain ON t_link (domain_id); -- 事件驱动批量失效:按域名定位
