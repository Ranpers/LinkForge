# LinkForge

LinkForge 是一个基于 Java 25 的短链接微服务平台，覆盖短链创建、跳转解析、状态管理、身份认证、授权决策和跨服务安全控制同步。项目使用 Spring Boot 4、Spring Cloud Gateway、Nacos、PostgreSQL、Redis 与 Kafka，并通过 OpenTelemetry 提供跨服务链路追踪。

## 主要能力

- Gateway 统一路由、JWT 校验和分场景 Redis 令牌桶限流
- OAuth2/OIDC 授权服务器、用户注册、RBAC 与域名授权
- 短链接创建、解析、启停和目标地址更新
- Redis 缓存与数据库回源，缓存失效时保持正确性
- IAM 事务内写入 Outbox，异步发布安全控制事件，Link 幂等消费
- JSON Schema 契约、Flyway 迁移与 Javadoc DocLint 校验
- Nacos 服务注册、逻辑服务名调用和按环境配置导入
- Docker Compose 完整开发栈与 OpenTelemetry/Grafana 可观测环境

## 项目结构

```text
LinkForge
├── contracts/                     # 服务间 JSON Schema 契约
├── deploy/
│   ├── compose/                   # 基础设施与完整应用编排
│   └── docker/                    # Java 25 非 root 应用镜像
├── docs/                          # 开发与运行文档
└── services/
    ├── linkforge-gateway/         # 统一入口、鉴权、路由、限流
    ├── linkforge-iam-service/     # 身份认证、授权、安全控制、Outbox
    └── linkforge-link-service/    # 短链接核心业务与控制事件消费
```

## 快速开始

### 环境要求

- JDK 25
- Docker Engine 与 Docker Compose（Windows 可使用 WSL 内的 Docker）
- Git

项目包含 Maven Wrapper，无需单独安装 Maven。

### 准备环境变量

从仓库根目录复制示例文件：

```bash
cp deploy/compose/.env.example deploy/compose/.env
```

填写 `.env` 中的密码和客户端密钥。分别生成 Nacos 令牌和 IAM 签名私钥加密密钥：

```bash
openssl rand -base64 64
openssl rand -base64 32
```

第二条命令的输出填写到 `IAM_SIGNING_KEY_ENCRYPTION_KEY`。已有 IAM 数据库必须始终使用同一加密密钥，否则已存储的签名私钥密文无法解密。`.env` 已加入忽略规则，禁止提交真实密钥。示例中的 `{noop}` 密钥仅供本地开发，生产环境必须改用安全哈希或密钥管理服务。

首次启动时，Compose 会以 `NACOS_PASSWORD` 幂等初始化 Nacos 管理员；若持久卷已存在，则该密码必须与卷内管理员密码一致。PostgreSQL 也只在空数据卷首次创建用户，已有卷的密码不会因修改 `.env` 自动变化。

### 启动基础设施

只启动 PostgreSQL 与 Redis：

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml up -d
```

### 启动完整平台

完整 Profile 会构建并启动三个应用，以及 Nacos、Kafka 和 OpenTelemetry LGTM：

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml \
  --profile app up -d --build
```

主要入口：

| 地址                     | 用途                        |
|--------------------------|-----------------------------|
| `http://localhost:8080`  | Gateway 统一入口            |
| `http://localhost:9000`  | IAM 服务（本地诊断）        |
| `http://localhost:9002`  | Link 服务（本地诊断）       |
| `http://localhost:18080` | Nacos 控制台                |
| `http://localhost:3000`  | Grafana（Trace/Metric/Log） |

公开 HTTP API 的 OpenAPI 3.1 契约由 Gateway 提供：

```text
http://localhost:8080/openapi/linkforge-public-api-v1.yaml
```

用户注册使用 `POST /api/v1/users`。API 错误统一返回
`application/problem+json`，其中 `code` 是供客户端判断的稳定错误码，`traceId`
与响应头 `X-Trace-Id` 一致。原型阶段数据库结构以当前 `V1__baseline.sql`
为准，结构发生变化后应重建本地数据卷。

检查容器和就绪状态：

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml \
  --profile app ps
curl --fail http://localhost:8080/actuator/health/readiness
```

停止容器但保留数据卷：

```bash
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml \
  --profile app down
```

更详细的路由、安全、Nacos Data ID 和可观测配置见 [运行平台说明](docs/runtime-platform.md)。

## 构建与验证

Windows：

```powershell
.\mvnw.cmd clean verify
```

Linux、macOS 或 WSL：

```bash
./mvnw clean verify
```

`verify` 会运行 Javadoc DocLint 检查。该检查只作用于主代码，任何 Javadoc 格式、引用或标签警告都会中断构建。

## 开发约定

- 业务规则保持在领域层，不依赖数据库、消息系统或 HTTP 框架
- 入站/出站交互通过 Port 与 Adapter 隔离
- 对外契约、领域规则和特殊一致性语义提供可靠 Javadoc
- 内部接口不得经 Gateway 暴露；匿名限流键不得直接信任客户端转发头
- 提交前运行 `mvn verify`，确保文档检查通过

Javadoc 编写要求见 [Javadoc 编写规范](docs/javadoc-style.md)。

## 开源许可

项目基于 Apache License 2.0 开源。完整条款见 [LICENSE](LICENSE)。

Copyright © 2026 Ranpers.
