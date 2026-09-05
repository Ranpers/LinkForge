ALTER TABLE t_user_link_security_restriction
    ADD COLUMN source varchar(32) NOT NULL DEFAULT 'MANUAL',
    ADD CONSTRAINT ck_link_security_source
        CHECK (source IN ('MANUAL', 'ACCOUNT_SUSPENSION'));

CREATE UNIQUE INDEX uq_active_account_suspension_restriction
    ON t_user_link_security_restriction (user_id, source)
    WHERE active AND source = 'ACCOUNT_SUSPENSION';

WITH inserted_restriction AS (
    INSERT INTO t_user_link_security_restriction
        (id, user_id, mode, active, reason_code, source)
    SELECT uuidv7(),
           link_user.id,
           'ALL',
           TRUE,
           'ACCOUNT_SECURITY_SUSPENDED',
           'ACCOUNT_SUSPENSION'
    FROM t_user link_user
    WHERE link_user.status = 2
      AND link_user.deleted_at IS NULL
    ON CONFLICT DO NOTHING
    RETURNING user_id
),
revised_user AS (
    UPDATE t_user link_user
    SET link_security_revision = link_user.link_security_revision + 1,
        updated_at = now()
    FROM inserted_restriction
    WHERE link_user.id = inserted_restriction.user_id
    RETURNING link_user.id AS user_id, link_user.link_security_revision AS revision
),
snapshot_event AS (
    SELECT revised_user.user_id,
           revised_user.revision,
           uuidv7() AS event_id,
           now() AS occurred_at
    FROM revised_user
)
INSERT INTO t_outbox_event
    (id, event_type, schema_version, stream_key, partition_key, trace_id, payload)
SELECT snapshot_event.event_id,
       'UserLinkSecurityRestrictionsChanged',
       1,
       'USER_LINK_SECURITY:' || snapshot_event.user_id::text,
       snapshot_event.user_id::text,
       NULL,
       jsonb_build_object(
           'eventId', snapshot_event.event_id::text,
           'eventType', 'UserLinkSecurityRestrictionsChanged',
           'schemaVersion', 1,
           'streamKey', 'USER_LINK_SECURITY:' || snapshot_event.user_id::text,
           'revision', snapshot_event.revision,
           'occurredAt', snapshot_event.occurred_at,
           'traceId', NULL,
           'payload', jsonb_build_object(
               'userId', snapshot_event.user_id::text,
               'restrictions', (
                   SELECT COALESCE(
                       jsonb_agg(
                           jsonb_build_object(
                               'restrictionId', restriction.id::text,
                               'mode', restriction.mode,
                               'rangeStart', restriction.range_start,
                               'rangeEnd', restriction.range_end,
                               'reasonCode', restriction.reason_code,
                               'createdAt', restriction.created_at
                           )
                           ORDER BY restriction.created_at, restriction.id
                       ),
                       '[]'::jsonb
                   )
                   FROM t_user_link_security_restriction restriction
                   WHERE restriction.user_id = snapshot_event.user_id
                     AND restriction.active
               )
           )
       )
FROM snapshot_event;
