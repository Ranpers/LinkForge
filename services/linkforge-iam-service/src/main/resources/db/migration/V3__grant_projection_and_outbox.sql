-- =============================================================
-- V3__grant_projection_and_outbox.sql — 授权投影与事件外发基建
--
-- 设计约定(详见《LinkForge 授权事件契约》):
--   1) t_user_domain_grant_state 只表达四路授权并集,不含用户/域名状态
--      (创建权限 = 用户正常 AND 域名启用 AND granted,三态分离,位不重叠)
--   2) granted=false 行永久保留(tombstone):revision 必须流内单调,
--      删行重授会让消费者检查点永久丢弃新授权事件
--   3) 三条状态流版本各自独立维护,契约只承诺同一 streamKey 内单调递增
--   4) 业务变更与 Outbox 事件同事务写入;投递失败重试,上限后 parked
-- =============================================================

-- ---------- 授权投影(用户×域名 grant 状态流) ----------

CREATE TABLE t_user_domain_grant_state
(
    user_id    uuid        NOT NULL REFERENCES t_user (id) ON DELETE RESTRICT,
    domain_id  uuid        NOT NULL REFERENCES t_domain (id) ON DELETE RESTRICT,
    granted    boolean     NOT NULL DEFAULT FALSE, -- 四路授权并集的当前值;false 行永久保留
    revision   bigint      NOT NULL DEFAULT 0,     -- 不存在/初始流=0;翻转才 +1
    updated_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, domain_id),
    CONSTRAINT ck_udgs_revision CHECK (revision >= 0)
);
CREATE INDEX idx_udgs_domain ON t_user_domain_grant_state (domain_id); -- 按域名扫描授权投影;受影响集合仍由六张授权关系表计算

-- ---------- 状态流版本列(用户/域名可用性流) ----------

-- 授权之外的两条事件流,版本挂各自实体行;仅状态实际变化时事务内 +1
ALTER TABLE t_user
    ADD COLUMN state_revision bigint NOT NULL DEFAULT 1;
ALTER TABLE t_user
    ADD CONSTRAINT ck_user_state_revision CHECK (state_revision >= 1);
COMMENT ON COLUMN t_user.state_revision IS '用户可用性状态流版本:最终 enabled 状态实际翻转时事务内 +1,事件据此保序';

ALTER TABLE t_domain
    ADD COLUMN state_revision bigint NOT NULL DEFAULT 1;
ALTER TABLE t_domain
    ADD CONSTRAINT ck_domain_state_revision CHECK (state_revision >= 1);
COMMENT ON COLUMN t_domain.state_revision IS '域名可用性状态流版本:最终 enabled 状态实际翻转时事务内 +1,事件据此保序';

-- ---------- 事务性 Outbox ----------

CREATE TABLE t_outbox_event
(
    -- 显式传入,必须等于 payload.eventId(同一事件只生成一次标识,
    -- 见契约 §7);不设默认值——随机 id 无法通过信封一致性 CHECK
    id            uuid PRIMARY KEY,
    event_type    varchar(64)  NOT NULL,   -- DomainAvailabilityChanged / UserAvailabilityChanged / UserDomainGrantChanged
    stream_key    varchar(160) NOT NULL,   -- DOMAIN:{domainId} / USER:{userId} / USER_DOMAIN:{userId}:{domainId}
    partition_key varchar(64)  NOT NULL,   -- Kafka 分区键:域名流与授权流用 domainId,用户流用 userId
    payload       jsonb        NOT NULL,   -- 完整事件(信封+目标状态),投递即 Kafka 消息体
    status        smallint     NOT NULL DEFAULT 0, -- 0=待投递 1=已投递 2=parked(重试上限,需人工)
    retry_count   int          NOT NULL DEFAULT 0,
    next_retry_at timestamptz,             -- NULL=立即可投递
    created_at    timestamptz  NOT NULL DEFAULT now(),
    sent_at       timestamptz,
    CONSTRAINT ck_outbox_status CHECK (status IN (0, 1, 2)),
    CONSTRAINT ck_outbox_retry_count CHECK (retry_count >= 0),
    CONSTRAINT ck_outbox_payload_envelope CHECK (
        jsonb_typeof(payload) = 'object'
        AND payload ?& ARRAY['eventId', 'eventType', 'streamKey', 'revision', 'occurredAt']
        AND (payload ->> 'eventId') IS NOT DISTINCT FROM id::text
        AND (payload ->> 'eventType') IS NOT DISTINCT FROM event_type
        AND (payload ->> 'streamKey') IS NOT DISTINCT FROM stream_key
        AND jsonb_typeof(payload -> 'revision') = 'number'
        AND jsonb_typeof(payload -> 'occurredAt') = 'string'
    ),
    CONSTRAINT ck_outbox_sent_state CHECK (
        (status = 1 AND sent_at IS NOT NULL)
        OR (status IN (0, 2) AND sent_at IS NULL)
    )
);
CREATE INDEX idx_outbox_dispatch ON t_outbox_event (status, next_retry_at); -- 投递器轮询:FOR UPDATE SKIP LOCKED 认领

-- ---------- 存量授权回填 ----------
-- 四路并集初始化投影;只建行不发事件(消费者尚不存在,当前事实即起点)。
-- 空库时为 no-op;有存量数据的库由本迁移一次性补齐,不走应用启动逻辑。

INSERT INTO t_user_domain_grant_state (user_id, domain_id, granted, revision)
SELECT user_id, domain_id, TRUE, 1
FROM (
         -- 路径1:用户角色 → 角色直绑域名
         SELECT ur.user_id, rd.domain_id
         FROM t_user_role ur
                  JOIN t_role_domain rd ON rd.role_id = ur.role_id
         UNION
         -- 路径2:用户角色 → 角色绑域名组 → 组内域名
         SELECT ur.user_id, dgd.domain_id
         FROM t_user_role ur
                  JOIN t_role_domain_group rdg ON rdg.role_id = ur.role_id
                  JOIN t_domain_group_domain dgd ON dgd.domain_group_id = rdg.domain_group_id
         UNION
         -- 路径3:用户直绑域名
         SELECT ud.user_id, ud.domain_id
         FROM t_user_domain ud
         UNION
         -- 路径4:用户绑域名组 → 组内域名
         SELECT udg.user_id, dgd.domain_id
         FROM t_user_domain_group udg
                  JOIN t_domain_group_domain dgd ON dgd.domain_group_id = udg.domain_group_id
     ) grant_pairs;
