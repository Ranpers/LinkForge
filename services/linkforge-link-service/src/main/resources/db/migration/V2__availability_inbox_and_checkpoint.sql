-- =============================================================
-- V2__availability_inbox_and_checkpoint.sql — 消费侧幂等基建与索引
--
-- 设计约定(详见《LinkForge 授权事件契约》):
--   1) disabled_reason 语义升级:8=用户不可用,与 2/4 同为事件驱动的
--      "当前阻断原因";复权自动清对应位,位 1(手动)永不被事件触碰
--   2) 消费三件事必须同事务:Inbox 去重 + 检查点推进 + disabled_reason 位更新
--   3) V1 已应用,本迁移只增不改
-- =============================================================

-- 域名撤权事件的精确失效定位:(user_id, domain_id)
CREATE INDEX idx_link_user_domain ON t_link (user_id, domain_id);

-- 位掩码语义升级(V1 注释无位 8;smallint 列定义无需变更)
COMMENT ON COLUMN t_link.disabled_reason IS
    '位掩码:0=正常 1=手动禁用 2=域名禁用 4=失去域权限 8=用户不可用(2/4/8 由事件驱动,复权清对应位)';

-- ---------- 消费幂等(Inbox) ----------
-- event_id 主键即幂等约束;重复投递在此拦截

CREATE TABLE t_inbox_event
(
    event_id     uuid PRIMARY KEY,          -- 事件信封 eventId,不本地生成
    event_type   varchar(64)  NOT NULL,
    stream_key   varchar(160) NOT NULL,    -- DOMAIN:{domainId} / USER:{userId} / USER_DOMAIN:{userId}:{domainId}
    processed_at timestamptz  NOT NULL DEFAULT now()
);

-- ---------- 状态流检查点 ----------
-- 消费端按流保存已应用版本;event.revision <= last_applied_revision 视为过期丢弃

CREATE TABLE t_stream_checkpoint
(
    stream_key            varchar(160) PRIMARY KEY,
    last_applied_revision bigint      NOT NULL DEFAULT 0,
    updated_at            timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_stream_checkpoint_revision CHECK (last_applied_revision >= 0)
);
