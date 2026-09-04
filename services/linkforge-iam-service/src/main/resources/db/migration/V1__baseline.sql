-- LinkForge IAM prototype baseline.
-- The prototype database is disposable: this file describes the final schema directly.

CREATE TABLE t_user
(
    id                     uuid PRIMARY KEY DEFAULT uuidv7(),
    username               varchar(64)  NOT NULL UNIQUE,
    password               varchar(255) NOT NULL,
    email                  varchar(128),
    real_name              varchar(64),
    status                 smallint     NOT NULL DEFAULT 1,
    link_security_revision bigint       NOT NULL DEFAULT 0,
    deleted_at             timestamptz,
    created_at             timestamptz  NOT NULL DEFAULT now(),
    updated_at             timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT ck_user_status CHECK (status IN (0, 1, 2)),
    CONSTRAINT ck_user_link_security_revision CHECK (link_security_revision >= 0)
);
COMMENT ON COLUMN t_user.status IS '0=DEACTIVATED, 1=ACTIVE, 2=SECURITY_SUSPENDED; deletion is represented by deleted_at';

CREATE TABLE t_role
(
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    code        varchar(32)  NOT NULL UNIQUE,
    name        varchar(64)  NOT NULL,
    description varchar(255),
    created_at  timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE t_permission
(
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    code        varchar(64)  NOT NULL UNIQUE,
    name        varchar(64)  NOT NULL,
    description varchar(255),
    created_at  timestamptz  NOT NULL DEFAULT now()
);

CREATE TABLE t_user_role
(
    user_id    uuid        NOT NULL REFERENCES t_user (id) ON DELETE CASCADE,
    role_id    uuid        NOT NULL REFERENCES t_role (id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);
CREATE INDEX idx_user_role_role_user ON t_user_role (role_id, user_id);

CREATE TABLE t_role_permission
(
    role_id       uuid        NOT NULL REFERENCES t_role (id) ON DELETE CASCADE,
    permission_id uuid        NOT NULL REFERENCES t_permission (id) ON DELETE CASCADE,
    created_at    timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (role_id, permission_id)
);
CREATE INDEX idx_role_permission_permission_role ON t_role_permission (permission_id, role_id);

CREATE TABLE t_domain
(
    id             uuid PRIMARY KEY DEFAULT uuidv7(),
    domain         varchar(253) NOT NULL UNIQUE,
    name           varchar(128),
    status         smallint     NOT NULL DEFAULT 1,
    state_revision bigint       NOT NULL DEFAULT 1,
    created_at     timestamptz  NOT NULL DEFAULT now(),
    updated_at     timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT ck_domain_status CHECK (status IN (0, 1)),
    CONSTRAINT ck_domain_state_revision CHECK (state_revision >= 1)
);
COMMENT ON COLUMN t_domain.domain IS 'Normalized lowercase short-link host without scheme, port, path, or trailing dot';

CREATE TABLE t_domain_group
(
    id         uuid PRIMARY KEY DEFAULT uuidv7(),
    code       varchar(64)  NOT NULL UNIQUE,
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
CREATE INDEX idx_domain_group_domain_domain_group ON t_domain_group_domain (domain_id, domain_group_id);

CREATE TABLE t_role_domain
(
    role_id    uuid        NOT NULL REFERENCES t_role (id) ON DELETE CASCADE,
    domain_id  uuid        NOT NULL REFERENCES t_domain (id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (role_id, domain_id)
);
CREATE INDEX idx_role_domain_domain_role ON t_role_domain (domain_id, role_id);

CREATE TABLE t_role_domain_group
(
    role_id         uuid        NOT NULL REFERENCES t_role (id) ON DELETE CASCADE,
    domain_group_id uuid        NOT NULL REFERENCES t_domain_group (id) ON DELETE CASCADE,
    created_at      timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (role_id, domain_group_id)
);
CREATE INDEX idx_role_domain_group_group_role ON t_role_domain_group (domain_group_id, role_id);

CREATE TABLE t_user_domain
(
    user_id    uuid        NOT NULL REFERENCES t_user (id) ON DELETE CASCADE,
    domain_id  uuid        NOT NULL REFERENCES t_domain (id) ON DELETE CASCADE,
    created_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, domain_id)
);
CREATE INDEX idx_user_domain_domain_user ON t_user_domain (domain_id, user_id);

CREATE TABLE t_user_domain_group
(
    user_id         uuid        NOT NULL REFERENCES t_user (id) ON DELETE CASCADE,
    domain_group_id uuid        NOT NULL REFERENCES t_domain_group (id) ON DELETE CASCADE,
    created_at      timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, domain_group_id)
);
CREATE INDEX idx_user_domain_group_group_user ON t_user_domain_group (domain_group_id, user_id);

CREATE TABLE t_user_link_security_restriction
(
    id          uuid PRIMARY KEY DEFAULT uuidv7(),
    user_id     uuid         NOT NULL REFERENCES t_user (id) ON DELETE RESTRICT,
    mode        varchar(24)  NOT NULL,
    range_start timestamptz,
    range_end   timestamptz,
    active      boolean      NOT NULL DEFAULT TRUE,
    reason_code varchar(64)  NOT NULL,
    created_at  timestamptz  NOT NULL DEFAULT now(),
    revoked_at  timestamptz,
    CONSTRAINT ck_link_security_mode CHECK (mode IN ('ALL', 'CREATED_DURING')),
    CONSTRAINT ck_link_security_range CHECK (
        (mode = 'ALL' AND range_start IS NULL AND range_end IS NULL)
        OR (mode = 'CREATED_DURING' AND (range_start IS NOT NULL OR range_end IS NOT NULL)
            AND (range_start IS NULL OR range_end IS NULL OR range_start < range_end))
    ),
    CONSTRAINT ck_link_security_active CHECK (
        (active AND revoked_at IS NULL) OR (NOT active AND revoked_at IS NOT NULL)
    )
);
CREATE INDEX idx_user_link_security_active
    ON t_user_link_security_restriction (user_id, created_at)
    WHERE active;

CREATE TABLE t_authorization_jwk
(
    key_id          varchar(64) PRIMARY KEY,
    algorithm       varchar(16) NOT NULL,
    public_key_der  text        NOT NULL,
    private_key_der text        NOT NULL,
    status          smallint    NOT NULL DEFAULT 1,
    created_at      timestamptz NOT NULL DEFAULT now(),
    retired_at      timestamptz,
    CONSTRAINT ck_authorization_jwk_algorithm CHECK (algorithm = 'RS256'),
    CONSTRAINT ck_authorization_jwk_status CHECK (status IN (0, 1)),
    CONSTRAINT ck_authorization_jwk_retired CHECK (
        (status = 1 AND retired_at IS NULL) OR (status = 0 AND retired_at IS NOT NULL)
    )
);
CREATE UNIQUE INDEX uq_authorization_jwk_active ON t_authorization_jwk ((status)) WHERE status = 1;

CREATE TABLE t_outbox_event
(
    id              uuid PRIMARY KEY,
    event_type      varchar(64)   NOT NULL,
    schema_version  integer       NOT NULL,
    stream_key      varchar(160)  NOT NULL,
    partition_key   varchar(64)   NOT NULL,
    trace_id        varchar(64),
    payload         jsonb         NOT NULL,
    status          smallint      NOT NULL DEFAULT 0,
    retry_count     integer       NOT NULL DEFAULT 0,
    next_retry_at   timestamptz,
    last_attempt_at timestamptz,
    last_error      varchar(2000),
    created_at      timestamptz   NOT NULL DEFAULT now(),
    sent_at         timestamptz,
    CONSTRAINT ck_outbox_schema_version CHECK (schema_version >= 1),
    CONSTRAINT ck_outbox_status CHECK (status IN (0, 1, 2)),
    CONSTRAINT ck_outbox_retry_count CHECK (retry_count >= 0),
    CONSTRAINT ck_outbox_sent_state CHECK (
        (status = 1 AND sent_at IS NOT NULL) OR (status IN (0, 2) AND sent_at IS NULL)
    )
);
CREATE INDEX idx_outbox_dispatch ON t_outbox_event (status, next_retry_at, created_at);

INSERT INTO t_role (code, name, description)
VALUES ('USER', '普通用户', '管理自己有权使用的域名下创建的资源'),
       ('NORMAL_ADMIN', '普通管理员', '日常域名与短链运营管理'),
       ('SYSTEM_ADMIN', '系统管理员', '系统全部管理能力');

INSERT INTO t_permission (code, name)
VALUES ('user:read', '查看个人信息'),
       ('user:update', '维护个人信息'),
       ('user:manage', '管理用户'),
       ('group:create', '创建分组'),
       ('group:read', '查看分组'),
       ('group:update', '编辑分组'),
       ('group:delete', '删除分组'),
       ('link:create', '创建短链'),
       ('link:read', '查看短链'),
       ('link:update', '编辑自己的短链'),
       ('link:delete', '删除自己的短链'),
       ('link:manage:any', '管理任意短链'),
       ('stats:read', '查看统计'),
       ('domain:create', '创建域名'),
       ('domain:read', '查看域名'),
       ('domain:update', '编辑域名'),
       ('domain:disable', '停用域名'),
       ('security:manage', '执行安全处置');

INSERT INTO t_role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM t_role role CROSS JOIN t_permission permission
WHERE role.code = 'USER'
  AND permission.code IN (
      'user:read', 'user:update',
      'group:create', 'group:read', 'group:update', 'group:delete',
      'link:create', 'link:read', 'link:update', 'link:delete',
      'stats:read'
  );

INSERT INTO t_role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM t_role role CROSS JOIN t_permission permission
WHERE role.code = 'NORMAL_ADMIN'
  AND permission.code IN (
      'user:read', 'group:read',
      'link:create', 'link:read', 'link:update', 'link:delete', 'link:manage:any',
      'stats:read',
      'domain:create', 'domain:read', 'domain:update', 'domain:disable'
  );

INSERT INTO t_role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM t_role role CROSS JOIN t_permission permission
WHERE role.code = 'SYSTEM_ADMIN';

INSERT INTO t_domain (id, domain, name)
VALUES ('01991d2e-0000-7000-8000-000000000001', 'go.linkforge.dev', '原型演示域名');

INSERT INTO t_role_domain (role_id, domain_id)
SELECT role.id, domain.id
FROM t_role role CROSS JOIN t_domain domain
WHERE role.code = 'USER' AND domain.domain = 'go.linkforge.dev';
