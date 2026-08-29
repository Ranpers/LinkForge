-- 授权服务器签名密钥。私钥属于高价值凭据，生产环境须依赖数据库访问控制和备份加密保护。
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
        (status = 1 AND retired_at IS NULL)
        OR (status = 0 AND retired_at IS NOT NULL)
    )
);

-- 当前阶段只允许一把活动签名密钥；保留历史行结构以支持后续轮换验证窗口。
CREATE UNIQUE INDEX uq_authorization_jwk_active
    ON t_authorization_jwk ((status))
    WHERE status = 1;
