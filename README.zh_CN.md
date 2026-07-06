# webnei

语言：[English](README.md) | [中文](README.zh_CN.md)

Spring Boot + MyBatis-Plus 后端 + Vue 3 / Vite 前端，用于浏览 WebNEI 导出数据 —
当前包括数据集、物品和流体。

> 后端是只读查询服务。数据库必须先由 exporter 灌入数据，API 才能提供内容。

## 架构

```text
PostgreSQL（只读）
  -> Spring Boot + MyBatis-Plus（导出表与读模型视图）
  -> JSON API（/api/**）
  -> Vue 3 + Vite SPA（pnpm）
```

- 后端是**只读查询服务**，基于 MyBatis-Plus mapper 映射到导出表和读模型视图。
- 前端是独立 Vite 项目，位于 `ui/`。
- 静态资源（渲染图标、纹理）**不由** Spring Boot 提供。API 返回每个资源的
  URL/路径，前端直接使用该值。部署时应通过 nginx、静态文件服务器或 CDN 保证
  返回的 URL 可访问。

## 环境要求

- Java 21
- 已灌入 WebNEI 导出 schema 的 PostgreSQL
- Node.js 22+ 及 pnpm

## 配置

所有设置均可通过环境变量覆盖。

| 变量 | 默认值 | 用途 |
| --- | --- | --- |
| `WEBNEI_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/webnei` | PostgreSQL 数据库连接 URL |
| `WEBNEI_DATASOURCE_USERNAME` | `postgres` | 数据库用户 |
| `WEBNEI_DATASOURCE_PASSWORD` | `postgres` | 数据库密码 |
| `WEBNEI_DEFAULT_DATASET_ID` | 空 | 可选的默认数据集 ID |
| `WEBNEI_ASSETS_PUBLIC_URL` | `/assets` | API 返回的资源 URL 前缀 |

以上默认值假设 PostgreSQL 运行在本地 `localhost:5432`。开发时如需连接远程数据库，
可在运行时设置环境变量：

```bash
WEBNEI_DATASOURCE_URL='jdbc:postgresql://<host>:5432/webnei' \
WEBNEI_DATASOURCE_USERNAME='<user>' \
WEBNEI_DATASOURCE_PASSWORD='<password>' \
./gradlew bootRun
```

也可创建 `src/main/resources/application-local.yaml` 写入自己的配置值，
然后激活该 profile：

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## API

所有端点均为只读，位于 `/api` 下。

| 控制器 | 端点 |
| --- | --- |
| **Datasets** | `GET /api/datasets`、`GET /api/datasets/default` |
| **Catalog items** | `GET /api/datasets/{datasetId}/items?q=&page=&size=` |
| **Catalog fluids** | `GET /api/datasets/{datasetId}/fluids?q=&page=&size=` |

## 资源 URL

资源路径由后端在 API DTO 中返回，前端不会改写或重新拼接。

通过 `WEBNEI_ASSETS_PUBLIC_URL` 控制后端返回的资源 URL 前缀。它可以是
`/assets` 这样的相对路径，也可以是 CDN/静态文件源站这样的绝对 URL。使用默认值时，
后端生成的 URL 格式：

```text
/assets/{packSlug}/{packVersion}/{variant}/{assetPath}
```

## 开发

启动后端（需要已灌入数据的 PostgreSQL）：

```bash
cd webnei
./gradlew bootRun
```

启动前端开发服务器：

```bash
cd webnei/ui
pnpm install
pnpm dev
```

Vite 开发服务器将 `/api` 代理到 `localhost:8080`。

验证后端健康状态：

```bash
curl http://localhost:8080/api/datasets
```

## 构建

```bash
./gradlew build
```

Gradle 构建会执行 `vue-tsc` 类型检查、Vite 生产构建，并将 `ui/dist`
复制到 Spring Boot 静态资源中以生成 fat JAR。

## 检查

| 命令 | 用途 |
| --- | --- |
| `./gradlew compileJava` | 编译后端 |
| `pnpm typecheck` | 前端类型检查 |
| `pnpm lint` | 前端 lint |
| `pnpm build` | 前端生产构建 |
