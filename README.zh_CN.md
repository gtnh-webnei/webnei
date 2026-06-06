# webnei

语言：[English](README.md) | [中文](README.zh_CN.md)

Spring Boot + JPA 后端 + Vue 3 / Vite 前端，用于浏览 NESQL 导出数据 —
物品、流体、怪物、配方、任务、Thaumcraft 要素、GT 矿石/世界生成数据。

## 架构

```text
PostgreSQL（只读）
  -> Spring Boot + JPA（读模型视图与实体）
  -> JSON API（/api/**）
  -> Vue 3 + Vite SPA（pnpm）
```

- 后端是**只读查询服务**，基于 JPA 实体映射到 NESQL 导出表和读模型视图。
- 前端是独立 Vite 项目，位于 `ui/`。
- 静态资源（渲染图标、纹理）**不由** Spring Boot 提供。开发时 Vite 中间件
  从本地导出目录提供 `/assets/**`，生产环境由 nginx 或静态文件服务器处理。

## 环境要求

- Java 21
- 装有 `pg_trgm` 扩展的 PostgreSQL（NESQL schema 需要）
- Node.js 22+ 及 pnpm

## 配置

所有设置均可通过环境变量覆盖。

| 变量 | 默认值 | 用途 |
| --- | --- | --- |
| `WEBNEI_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/nesql` | NESQL 数据库连接 URL |
| `WEBNEI_DATASOURCE_USERNAME` | `postgres` | 数据库用户 |
| `WEBNEI_DATASOURCE_PASSWORD` | `postgres` | 数据库密码 |
| `WEBNEI_ASSETS_PUBLIC_URL` | `/assets` | 资源链接的公开 URL 前缀 |
| `WEBNEI_CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | CORS 允许的来源 |

以上默认值假设 PostgreSQL 运行在本地 `localhost:5432`。开发时如需连接远程数据库，
可在运行时设置环境变量：

```bash
WEBNEI_DATASOURCE_URL='jdbc:postgresql://<host>:5432/nesql' \
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
| **Datasets** | `GET /api/datasets`、`/api/datasets/{id}`、`/api/datasets/{id}/mods/page` |
| **Items** | `GET .../items?q=&modId=&page=&size=`、`.../items/{variantId}`、`.../mods` |
| **Fluids** | `GET .../fluids?q=&modId=&page=&size=`、`.../fluids/{variantId}`、`.../fluid-mods` |
| **Mobs** | `GET .../mobs?q=&modId=&page=&size=`、`.../mobs/{variantId}`、`.../mobs/mods` |
| **Recipes** | `GET .../recipe-categories/page?q=&modId=&hideEmpty=`、`.../recipes/lookup?target=&kind=`、`.../recipes/lookup/breakdown`、`.../recipes/{id}`、`.../categories/{id}/recipes`、`.../categories/{id}/machines`、`.../categories/{id}/voltage-tiers` |
| **Quests** | `GET .../quest-lines`、`.../quest-lines/{id}`、`.../quests/{id}` |
| **Extras** | `GET .../items/{id}/extras`、`.../items/{id}/containers`、`.../fluids/{id}/extras` |
| **GT Ore** | `GET .../gt/ore-veins?q=&dimension=`、`.../gt/small-ores`、`.../gt/underground-fluids`、`.../gt/bartworks-ores`，及各详情端点 |

## 资源目录布局

将 NESQL 导出包解压到 `WEBNEI_ASSETS_DIR` 指向的目录（Vite 开发服务器使用）：

```text
{存放目录}/
  {pack_slug}/{pack_version}/{variant}/{language}/
    item/...   （渲染的物品图标）
    mob/...    （实体渲染图）
    spec/...   （展示 spec 文件）
    ...
```

后端生成的资源 URL 格式：

```text
/assets/{packSlug}/{packVersion}/{variant}/{language}/{assetPath}?v={sha256}
```

## 开发

启动后端（需要已灌入数据的 PostgreSQL）：

```bash
cd webnei
./gradlew bootRun
```

启动前端开发服务器（需要设置 `WEBNEI_ASSETS_DIR`）：

```bash
cd webnei/ui
pnpm install
WEBNEI_ASSETS_DIR=/path/to/exports pnpm dev
```

Vite 开发服务器将 `/api` 代理到 `localhost:8080`，并从本地导出目录
提供 `/assets/**`。

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
