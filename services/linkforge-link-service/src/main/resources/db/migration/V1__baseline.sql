-- LinkForge Link prototype baseline.
-- Runtime control facts are projected into small tables; link rows are never mass-updated.

CREATE TABLE t_domain_state
(
    domain_id  uuid PRIMARY KEY,
    host       varchar(253) NOT NULL UNIQUE,
    enabled    boolean      NOT NULL,
    revision   bigint       NOT NULL,
    updated_at timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT ck_domain_state_revision CHECK (revision >= 1),
    CONSTRAINT ck_domain_state_host CHECK (host = lower(host) AND host !~ '[/:]')
);

CREATE TABLE t_group
(
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id    uuid        NOT NULL,
    name       varchar(64) NOT NULL,
    sort_order integer     NOT NULL DEFAULT 0,
    deleted_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_group_user ON t_group (user_id, sort_order);

CREATE TABLE t_link
(
    id                  uuid PRIMARY KEY DEFAULT uuidv7(),
    created_by_user_id  uuid          NOT NULL,
    group_id            uuid          REFERENCES t_group (id) ON DELETE SET NULL,
    name                varchar(64)   NOT NULL,
    link_code           varchar(32)   NOT NULL,
    code_type           varchar(16)   NOT NULL,
    full_url            varchar(2048) NOT NULL,
    sort_order          integer       NOT NULL DEFAULT 0,
    domain_id           uuid          NOT NULL REFERENCES t_domain_state (domain_id) ON DELETE RESTRICT,
    status              varchar(16)   NOT NULL DEFAULT 'ACTIVE',
    disabled_reason_code varchar(64),
    expires_at          timestamptz,
    idempotency_key     varchar(128),
    request_fingerprint char(64),
    revision            bigint        NOT NULL DEFAULT 1,
    deleted_at          timestamptz,
    created_at          timestamptz   NOT NULL DEFAULT now(),
    updated_at          timestamptz   NOT NULL DEFAULT now(),
    CONSTRAINT uq_link_domain_code UNIQUE (domain_id, link_code),
    CONSTRAINT ck_link_code CHECK (
        (code_type = 'GENERATED' AND link_code ~ '^[0-9A-Za-z]{10}$')
        OR (code_type = 'CUSTOM' AND link_code ~ '^[A-Za-z0-9_-]{4,32}$')
    ),
    CONSTRAINT ck_link_status CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT ck_link_disabled_reason CHECK (
        (status = 'ACTIVE' AND disabled_reason_code IS NULL)
        OR (status = 'DISABLED' AND disabled_reason_code IS NOT NULL)
    ),
    CONSTRAINT ck_link_expiry CHECK (expires_at IS NULL OR expires_at > created_at),
    CONSTRAINT ck_link_revision CHECK (revision >= 1),
    CONSTRAINT ck_link_idempotency CHECK (
        (idempotency_key IS NULL AND request_fingerprint IS NULL)
        OR (idempotency_key IS NOT NULL AND request_fingerprint IS NOT NULL)
    )
);
CREATE INDEX idx_link_creator ON t_link (created_by_user_id, created_at DESC);
CREATE INDEX idx_link_group ON t_link (group_id, sort_order);
CREATE INDEX idx_link_domain_created ON t_link (domain_id, created_at DESC);
CREATE UNIQUE INDEX uq_link_creator_idempotency
    ON t_link (created_by_user_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE TABLE t_user_link_security_restriction
(
    restriction_id uuid PRIMARY KEY,
    user_id         uuid         NOT NULL,
    mode            varchar(24)  NOT NULL,
    range_start     timestamptz,
    range_end       timestamptz,
    reason_code     varchar(64)  NOT NULL,
    created_at      timestamptz  NOT NULL,
    source_revision bigint       NOT NULL,
    CONSTRAINT ck_link_security_mode CHECK (mode IN ('ALL', 'CREATED_DURING')),
    CONSTRAINT ck_link_security_revision CHECK (source_revision >= 1),
    CONSTRAINT ck_link_security_range CHECK (
        (mode = 'ALL' AND range_start IS NULL AND range_end IS NULL)
        OR (mode = 'CREATED_DURING' AND (range_start IS NOT NULL OR range_end IS NOT NULL)
            AND (range_start IS NULL OR range_end IS NULL OR range_start < range_end))
    )
);
CREATE INDEX idx_link_security_user ON t_user_link_security_restriction (user_id, created_at);

CREATE TABLE t_inbox_event
(
    event_id       uuid PRIMARY KEY,
    event_type     varchar(64)  NOT NULL,
    schema_version integer      NOT NULL,
    stream_key     varchar(160) NOT NULL,
    trace_id       varchar(64),
    processed_at   timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT ck_inbox_schema_version CHECK (schema_version >= 1)
);

CREATE TABLE t_stream_checkpoint
(
    stream_key            varchar(160) PRIMARY KEY,
    last_applied_revision bigint      NOT NULL DEFAULT 0,
    updated_at            timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_stream_checkpoint_revision CHECK (last_applied_revision >= 0)
);

INSERT INTO t_domain_state (domain_id, host, enabled, revision)
VALUES ('01991d2e-0000-7000-8000-000000000001', 'go.linkforge.dev', TRUE, 1);
