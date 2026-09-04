# LinkForge

LinkForge 是一个面向现代应用场景的短链接服务项目，目标是提供清晰、可靠且便于持续演进的链接管理能力。

项目围绕短链接的完整生命周期展开，涵盖链接创建、访问解析、状态管理、身份认证与安全控制等基础能力。当前仍处于持续开发阶段，接口与部署方式可能随版本演进而调整。

## 主要能力

- 创建并管理短链接
- 将短链接快速解析到目标地址
- 支持链接启用、停用与目标地址更新
- 提供用户注册、身份认证与访问授权
- 支持域名状态和用户安全策略管理
- 在服务之间同步链接控制信息
- 为高频访问场景提供缓存支持

## 项目结构

```text
LinkForge
├── contracts/                     # 服务间共享契约
├── deploy/                        # 本地开发环境配置
├── docs/                          # 项目文档与开发规范
└── services/
    ├── linkforge-gateway/         # 网关模块（预留）
    ├── linkforge-iam-service/     # 身份认证与权限管理
    └── linkforge-link-service/    # 短链接核心业务
```

## 快速开始

### 环境要求

- JDK 25
- Docker 与 Docker Compose
- Git

项目已经包含 Maven Wrapper，无需单独安装 Maven。

### 获取项目

```bash
git clone https://github.com/Ranpers/LinkForge.git
cd LinkForge
```

### 准备本地环境

复制环境变量示例文件，并根据本机环境修改其中的配置：

```powershell
Copy-Item deploy/compose/.env.example deploy/compose/.env
```

启动默认的本地基础设施：

```powershell
docker compose --env-file deploy/compose/.env -f deploy/compose/docker-compose.yml up -d
```

其他可选组件可通过 Compose Profile 按需启用，具体说明见部署配置文件。

### 构建与验证

Windows：

```powershell
.\mvnw.cmd clean verify
```

Linux 或 macOS：

```bash
./mvnw clean verify
```

构建通过后，可分别启动身份认证服务与短链接服务。首次运行前，请根据各服务的配置文件补充必要的本地环境变量。

## 开发约定

- 保持模块边界清晰，业务规则不依赖具体基础设施
- 新增或修改行为时同步补充测试
- 对外契约、领域规则和特殊一致性语义应提供可靠的 Javadoc
- 提交代码前运行 `mvn verify`，确保测试与文档检查通过

Javadoc 编写要求见 [Javadoc 编写规范](docs/javadoc-style.md)。

## 项目状态

LinkForge 目前处于早期开发阶段，主要能力仍在逐步完善中。欢迎通过 Issue 提交问题、建议或改进思路。

## 参与贡献

欢迎参与项目建设。提交改动前，请尽量保持变更范围单一、说明清晰，并确保相关验证可以通过。

## 版权

Copyright © 2026 Ranpers. All rights reserved.

详细说明见 [COPYRIGHT](COPYRIGHT)。
