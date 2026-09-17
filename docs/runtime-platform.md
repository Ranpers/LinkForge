# 运行平台说明

本文说明 Gateway、Nacos、容器网络与 OpenTelemetry 的运行约定。

## Gateway 边界

Gateway 只暴露明确的公共接口：

| 路由                 | 下游服务                 | 接口范围                 | 认证       |
|----------------------|--------------------------|--------------------------|------------|
| `iam-authentication` | `linkforge-iam-service`  | 注册、OAuth2、OIDC、登录 | 按协议公开 |
| `iam-api`            | `linkforge-iam-service`  | 用户与域名管理           | JWT        |
| `link-api`           | `linkforge-link-service` | 链接与分组管理           | JWT        |
| `link-redirect`      | `linkforge-link-service` | `/r/**`                  | 公开       |

`/internal/**` 在网关安全链中直接拒绝，服务间授权接口只能在内部网络访问。认证、业务 API、跳转分别使用独立的 Redis 令牌桶参数。已认证请求以 JWT Subject 作为限流键；匿名请求使用直接对端地址，不默认信任 `X-Forwarded-For`。

Gateway 不信任客户端传入的 `X-Request-Id`，而是在安全过滤器前生成新的关联标识，
传递给下游并写入响应头。IAM 与 Link Service 只沿用规范 UUID 形式的入站标识，其余情况
自行生成；Link Service 调用 IAM 时会原样传递当前标识，使跨服务调用共用同一个值。
除 406 外，所有 HTTP API 错误统一使用 `application/problem+json`，稳定业务码位于 `code`
字段。406 表示无法按 `Accept` 头生成响应，它不携带响应体，但仍携带 `X-Request-Id`；触发它的
`Accept` 头同样约束问题响应自身，写任何响应体都会让结果取决于协商结果，因此服务端不尝试
协商，调用方只依据状态码判断。
公开接口契约位于 `/openapi/linkforge-public-api-v1.yaml`，其中的路径都相对于 Gateway
地址，不声明固定的服务器地址。

Gateway 自身会返回 401、403、404、405、429、500、503、504：401 与 403 来自安全链，
404 表示没有匹配的路由，405 由下游服务判定后透传（Gateway 按路径转发，无法区分 405
与 404），429 表示超出 Redis 令牌桶配额，503 表示没有可用的下游实例，504 表示下游响应
超时。超时阈值由 `spring.cloud.gateway.server.webflux.httpclient.response-timeout`
控制，默认 10 秒，可用 `GATEWAY_RESPONSE_TIMEOUT` 覆盖；未配置时 504 分支不会触发。
下游服务另外会返回 400、406、409、415：406 表示无法按 `Accept` 头生成响应（无响应体，见上），
415 表示请求体的 `Content-Type` 不受支持。除 406 外的上述所有响应，无论由 Gateway 还是下游
服务产生，都带 `X-Request-Id` 响应头，并与响应体中的 `requestId` 一致。
公开契约按操作声明可达状态码，不做统一填充：503 适用于全部操作，401 适用于除用户注册与
短链跳转之外的全部操作 —— 只有这两个操作无需访问令牌，不会返回 401。400、403、404、409
只在具体业务会产生的操作上声明。

限流的四个 `X-RateLimit-Remaining`、`X-RateLimit-Replenish-Rate`、`X-RateLimit-Burst-Capacity`
和 `X-RateLimit-Requested-Tokens` 响应头在放行与拒绝两种结果上都会返回，因此公开契约把它们
与 `X-Request-Id` 一起声明在全部成功响应和 429 上；401 不声明，因为认证失败可能发生在限流
过滤器之前。这些响应头通过 CORS `Access-Control-Expose-Headers` 暴露给浏览器脚本。不返回
`Retry-After`：令牌桶按速率补充，不产生可供换算的固定等待时长。`X-RateLimit-Remaining` 取值
为 -1 表示本次限流判定已经降级（令牌桶依赖的存储不可用，网关按放行处理），该取值不反映真实余量。

如果 Gateway 位于受控反向代理之后，应在网络边界清洗转发头，再根据代理拓扑调整 `server.forward-headers-strategy` 和限流键解析逻辑。

Gateway 与 Link Service 同时校验访问令牌的签发者和 `linkforge-api` 受众。IAM 生成的所有访问令牌都写入该受众；OIDC ID Token 保持协议规定的客户端受众。IAM 签名私钥使用 `IAM_SIGNING_KEY_ENCRYPTION_KEY` 进行 AES-256-GCM 加密后再写入数据库，只接受当前 `enc:v1` 密文格式。该环境变量必须稳定保存，不能在已有数据卷上重新生成。

## Nacos 注册与配置

容器环境设置 `NACOS_ENABLED=true` 与 `NACOS_CONFIG_ENABLED=true`。服务注册在 `LINKFORGE` Group 中，调用方使用下列逻辑名称：

- `linkforge-iam-service`
- `linkforge-link-service`
- `linkforge-gateway`

Nacos 3 不再提供默认管理员密码。Compose 中的 `nacos-init` 一次性任务会先尝试登录；仅在管理员尚未初始化时使用 `NACOS_PASSWORD` 创建管理员，随后再次验证凭据。已有数据卷若使用其他密码会明确失败，不会静默改写管理员。

每个应用按顺序导入两个可选 Data ID：

1. `linkforge-common-${LINKFORGE_ENV}.yaml`
2. `${spring.application.name}-${LINKFORGE_ENV}.yaml`

默认容器环境名为 `container`。公共配置放在第一个 Data ID，服务私有配置放在第二个；两者使用 `LINKFORGE` Group。配置项仍应通过环境变量提供安全默认值，Nacos 不得保存明文生产密钥。

本地独立运行或不使用 Nacos 时保持两个开关为 `false`。配置导入带 `optional:`，Nacos 不可用不会破坏本地构建流程。

## Kafka 网络

Kafka 使用两套监听地址：

- 容器内应用：`kafka:29092`
- 宿主机调试工具：`localhost:9092`

应用容器不得使用 `localhost:9092`，否则 broker 返回的地址在容器网络内不可达。

## OpenTelemetry

三个服务均使用 Spring 管理的 HTTP 客户端和框架内置观测能力传播 W3C Trace Context。容器 Profile 将 Trace 通过 OTLP/HTTP 发送到 `otel-lgtm:4318/v1/traces`，Grafana 位于 `http://localhost:3000`。

关键开关：

| 环境变量                           | 默认值                            | 说明                       |
|------------------------------------|-----------------------------------|----------------------------|
| `OTEL_TRACES_ENABLED`              | `false`                           | 是否导出 Trace             |
| `OTEL_TRACES_SAMPLING_PROBABILITY` | `1.0`                             | 采样概率，范围 0 到 1      |
| `OTEL_EXPORTER_OTLP_ENDPOINT`      | `http://localhost:4318/v1/traces` | Trace OTLP/HTTP 地址       |
| `OTEL_METRICS_ENABLED`             | `false`                           | 是否启用 OTLP Metrics 导出 |

本地运行默认关闭 Trace 和 Metrics 导出，避免在 Collector 不存在时产生后台重试。完整 Compose Profile 显式启用 Trace；指标继续由应用 Actuator 暴露策略决定。

## 健康检查与排障

三个应用均暴露：

- `/actuator/health/liveness`
- `/actuator/health/readiness`

常用命令：

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml --profile app ps
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml --profile app logs gateway
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml --profile app logs iam-service link-service
```

若服务未注册，依次检查 Nacos 健康状态、用户名密码、`NACOS_SERVER_ADDR`、Group 和容器日志。若跨服务 HTTP 调用失败，确认逻辑服务名与 Nacos 中注册的 `spring.application.name` 完全一致。
