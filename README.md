# 智选云商城 Cloud

一个面向平台、商家和消费者的 B2B2C 微服务商城项目，基于开源项目 **Lilishop / PickMall** 二次开发。

## 仓库结构

```text
smart-mall-cloud
├── backend/                 # Java 微服务、网关、AI Helper、Docker 配置
│   ├── lili-api
│   ├── lili-common
│   ├── lili-gateway
│   ├── lili-service
│   ├── lili-AIHelper
│   ├── docker
│   └── pom.xml
├── frontend/                # Vue3 前端
│   ├── buyer                # 买家端
│   ├── seller               # 商家端
│   ├── manager              # 平台管理端
│   └── im                   # 客服 IM
└── README.md
```

## 后端启动

详细说明见 [backend/README.md](backend/README.md)。

```bash
cd backend
cp .env.example .env
cd docker
docker compose --env-file ../.env up -d
cd ..
mvn clean package -DskipTests
```

## 前端启动

详细说明和上线检查项见 [frontend/MIGRATION-VUE3.md](frontend/MIGRATION-VUE3.md)。

```bash
cd frontend/buyer
npm install
npm run dev
```

其他子项目按同样方式进入 `frontend/seller`、`frontend/manager`、`frontend/im` 启动。默认端口：

| 端 | 端口 |
| --- | ---: |
| buyer | 10000 |
| seller | 10002 |
| manager | 10003 |
| im | 8000 |

> `node_modules`、`dist`、本地 `.env` 和构建产物不纳入版本管理。

## 项目说明

- 后端模块、接口、启动顺序和环境变量见 [backend/README.md](backend/README.md)。
- 前端是四个独立 Vue3 + Element Plus 子项目，切换目录后分别安装依赖和启动。
- 项目继承上游 AGPL-3.0 许可证，版权和来源说明保留在 [LICENSE](LICENSE)。
