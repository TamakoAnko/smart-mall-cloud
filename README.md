# 智选云商城 Cloud

一个面向平台、商家和消费者的 B2B2C 微服务商城项目，覆盖会员、商品、购物车、订单、支付、售后、消息和 AI 购物助手等业务。

> 重要说明：本项目基于开源项目 **Lilishop / PickMall** 二次开发，不是 Lilishop 官方仓库，也不是完全从零自研的商业系统。
>
> 上游项目：
>
> - GitHub: <https://github.com/lilishop/lilishop>
> - Gitee: <https://gitee.com/beijing_hongye_huicheng/lilishop>
>
> 上游项目使用 AGPL-3.0 许可证，本仓库继续保留原许可证和版权说明。商业使用前请参考上游授权政策。

## 项目定位

本项目用于学习微服务拆分、统一鉴权、交易一致性、Redis 消息队列和 Java AI 应用开发。

我在开源商城基础上进行的主要改造包括：

- 按会员、商品、订单、消息和管理端等业务边界整合服务。
- 使用 Gateway 统一处理路由、Token 校验和用户上下文透传。
- 使用 Nacos 完成服务注册与配置。
- 梳理商品 SPU/SKU、购物车、交易单、订单、支付回调和卡密链路。
- 使用 Redis 实现缓存、订单级锁、轻量消息和延时任务。
- 为订单、库存、卡密和支付回调补充幂等与状态约束。
- 新增独立的 AI 购物助手服务，支持会话记忆、工具调用、RAG 和待确认写操作。
- 使用 AI 辅助生成部分脚手架、接口初稿、测试草稿和排查建议，再进行人工审查和联调。

## 功能模块

| 模块 | 主要能力 |
| --- | --- |
| 会员服务 | 注册、登录、令牌、会员信息、积分、收货地址、评价 |
| 商品服务 | 分类、品牌、SPU/SKU、规格、库存、上下架、搜索、卡密 |
| 订单服务 | 购物车、交易单、订单、支付、退款、售后、钱包 |
| 消息服务 | 站内消息、文件、物流、短信、文章页面 |
| 管理服务 | 平台端、商家端、统计、定时任务和异步消息消费 |
| AI 助手 | RAG、Function Calling、MCP、会话记忆和待确认操作 |
| 网关 | 统一入口、路由、JWT 校验、Redis 白名单和用户上下文透传 |

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言与构建 | Java 21、Maven |
| 基础框架 | Spring Boot 3.5、Spring Cloud 2025 |
| 微服务 | Nacos、Spring Cloud Gateway、OpenFeign、LoadBalancer |
| 数据库 | MySQL 8、MyBatis-Plus |
| 缓存与消息 | Redis、Redis List/ZSet 轻量消息 |
| 鉴权 | JWT、Redis Token 白名单、RBAC 与数据权限 |
| AI | Spring AI、DeepSeek、RAG、Function Calling、MCP、SSE |
| 部署 | Docker Compose、Nginx |

## 系统架构

```text
浏览器 / 移动端
       │
       ▼
Spring Cloud Gateway
  ├── Token 校验
  ├── 用户上下文透传
  └── 路由转发
       │
       ├──────────────┬──────────────┬──────────────┐
       ▼              ▼              ▼              ▼
 member-service  goods-service  order-service  message-service
       │              │              │              │
       └──────────────┴──────┬───────┴──────────────┘
                             ▼
                    MySQL + Redis + Nacos

manager-service
  ├── 平台与商家接口
  ├── Redis 消息消费者
  └── 定时任务

lili-AIHelper
  ├── ChatClient
  ├── Redis 会话记忆
  ├── RAG
  ├── Function Calling
  └── MCP Client
```

## 核心设计

### 1. 网关鉴权

1. 请求进入 Gateway。
2. 从配置的 Token Header 中读取访问令牌。
3. 校验 JWT 签名和有效期。
4. 检查 Redis 中是否存在有效 Token 白名单记录。
5. 将用户上下文和用户 ID 注入内部请求头。
6. 下游服务使用当前用户身份进行数据权限校验。

### 2. 订单与库存一致性

- 商品、订单和库存主状态以数据库为准。
- 下单时先完成必要的本地事务。
- 事务提交后再投递异步消息，避免产生脏消息。
- 库存扣减使用条件更新，确保“判断 + 扣减”是原子操作。
- 消费端使用订单号和状态机实现幂等，重复消息不会重复扣减。
- 未支付订单通过延时消息或定时任务取消，并释放库存和优惠权益。

### 3. Redis 轻量消息

改造后的项目使用 Redis 实现轻量消息能力：

- Redis List：普通任务队列。
- Redis ZSet：延时消息。
- Topic 路由：将消息分发给对应业务消费者。
- 事务提交后投递：避免数据库回滚后仍发送消息。

该轻量方案适合演示和低吞吐场景。生产环境如果要求更高的可靠性，应优先评估 RocketMQ 等成熟消息中间件。

### 4. AI 购物助手

- `ChatClient` 负责模型交互和流式输出。
- `@Tool` 暴露商品、订单、物流和购物车等受控能力。
- 用户 ID 不交给模型自由传入，而是从登录上下文获取。
- 查询工具可以直接执行，写操作先生成待确认操作，用户确认后由后端执行。
- Redis 保存会话记忆，上下文过长时执行摘要压缩。
- RAG 用于业务知识检索，实时数据通过 Function Calling 查询。
- MCP 用于标准化接入外部工具，不替代后端权限和参数校验。

## 项目结构

```text
smart-mall-cloud
├── lili-api              # 服务间调用契约
├── lili-common           # 公共工具、缓存、消息和安全组件
├── lili-gateway          # Spring Cloud Gateway
├── lili-service
│   ├── member-service    # 会员服务
│   ├── goods-service     # 商品服务
│   ├── order-service     # 订单服务
│   ├── message-service   # 消息服务
│   └── manager-service   # 管理和消息消费
├── lili-AIHelper         # AI 购物助手
├── docker                # Docker Compose 和数据库初始化
└── .env.example          # 环境变量模板
```

## 本地运行

### 1. 环境要求

- JDK 21。
- Maven 3.9+。
- Docker Desktop 或独立安装 MySQL、Redis、Nacos。
- Node.js 和 pnpm（如需启动前端）。

### 2. 配置环境变量

```bash
cp .env.example .env
```

至少需要设置：

- `MYSQL_ROOT_PASSWORD`
- `DB_PASSWORD`
- `REDIS_PASSWORD`
- `JWT_SECRET`
- `DEEPSEEK_API_KEY`

`JWT_SECRET` 必须是 Base64 编码的 HMAC 密钥，解码后长度不少于 32 字节。

### 3. 启动基础设施

```bash
cd docker
docker compose --env-file ../.env up -d
```

MySQL 容器启动时会自动执行 `docker/mysql/init/lilishop.sql`。

Nacos 控制台默认地址：

```text
http://127.0.0.1:8848/nacos
```

### 4. 编译后端

```bash
mvn clean package -DskipTests
```

### 5. 启动服务

建议按照以下顺序启动：

1. `lili-gateway`
2. `member-service`
3. `goods-service`
4. `order-service`
5. `message-service`
6. `manager-service`
7. `lili-AIHelper`

## 主要接口示例

```text
POST /buyer/passport/member/userLogin
GET  /buyer/passport/member
GET  /buyer/goods/category/get/{parentId}
GET  /buyer/order/...
POST /ai/conversations
POST /ai/chat
GET  /ai/conversations/{conversationId}/messages
POST /ai/actions/{actionId}/confirm
```

## 我的工作

我在开源商城基础上参与的改造包括：

- 网关统一鉴权、Token 校验和用户上下文透传。
- 服务模块整合和 API 契约统一。
- 订单、库存、卡密、支付回调相关链路梳理与幂等加固。
- Redis 缓存、消息队列、延时任务和并发控制。
- AI 购物助手、工具调用、会话记忆和待确认操作。
- 使用 AI 工具辅助生成和排查代码，并负责联调、修改和结果验证。

## 已知限制

- 项目基于开源商城二次开发，业务总量不能算作个人从零完成的代码量。
- 当前仓库使用 Redis 轻量消息，不能完全替代 RocketMQ 的可靠消息能力。
- RAG 默认关闭，启用前需要配置 Embedding 模型和知识库。
- 部分支付、短信、对象存储功能需要真实第三方账号才能完整验证。
- 自动化测试覆盖不足，项目没有生产环境流量数据。

## 开源协议

本项目继承上游 Lilishop 的 `AGPL-3.0` 许可证，原许可证文件位于仓库根目录的 `LICENSE`。

二次开发时已经：

- 保留上游版权和许可证说明。
- 在 README 中明确标注开源来源。
- 移除或替换发布副本中的真实密钥和本地环境信息。

如需闭源商用或二次分发，请先阅读 AGPL-3.0 条款并确认上游商业授权要求。

