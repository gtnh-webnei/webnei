# webnei

Language: [English](README.md) | [中文](README.zh_CN.md)

Spring Boot + MyBatis-Plus backend with a Vue 3 / Vite frontend for browsing
exported WebNEI data — currently datasets, items, fluids, and recipe
categories.

> The backend is a read-only query service. The database must be populated by
> the exporter before the API can serve data.

## Architecture

```text
PostgreSQL (read-only)
  -> Spring Boot + MyBatis-Plus (export tables & read-model views)
  -> JSON API (/api/**)
  -> Vue 3 + Vite SPA (pnpm)
```

- The backend is a **read-only query service** backed by MyBatis-Plus mappers
  mapped to exported tables and read-model views.
- The frontend is a separate Vite project in `ui/`.
- Static assets (rendered icons, textures) are **not** served by Spring Boot.
  The API returns each asset URL/path, and the frontend uses that value
  directly. Deployments should make the returned URLs resolvable via nginx, a
  static file server, or a CDN.

## Requirements

- Java 21
- PostgreSQL populated with the WebNEI export schema
- Node.js 22+ with pnpm

## Configuration

All settings can be overridden via environment variables.

| Variable | Default | Purpose |
| --- | --- | --- |
| `WEBNEI_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/webnei` | PostgreSQL database URL |
| `WEBNEI_DATASOURCE_USERNAME` | `postgres` | Database user |
| `WEBNEI_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `WEBNEI_DEFAULT_DATASET_ID` | empty | Optional default dataset id |
| `WEBNEI_ASSETS_PUBLIC_URL` | `/assets` | Asset URL prefix returned by the API |

The defaults assume a local PostgreSQL on `localhost:5432`. For development
against another database, either set the variables when running:

```bash
WEBNEI_DATASOURCE_URL='jdbc:postgresql://<host>:5432/webnei' \
WEBNEI_DATASOURCE_USERNAME='<user>' \
WEBNEI_DATASOURCE_PASSWORD='<password>' \
./gradlew bootRun
```

Or create `src/main/resources/application-local.yaml` with your own values,
then activate it:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## API

All endpoints are read-only and live under `/api`.

| Controller | Endpoint | Notes |
| --- | --- | --- |
| **Datasets** | `GET /api/dataset/list` | Returns dataset list plus `defaultId`; there is no separate `/api/dataset/default` endpoint |
| **Catalog items** | `POST /api/item/list` | JSON body: `datasetId`, `q`, `page`, `size` |
| **Catalog fluids** | `POST /api/fluid/list` | JSON body: `datasetId`, `q`, `page`, `size` |
| **Recipe categories** | `POST /api/recipe/categories/list` | JSON body: `datasetId`, `q`, `page`, `size` |

## Asset URLs

Asset paths are returned by the backend in API DTOs. The frontend does not
rewrite or reconstruct them.

Configure `WEBNEI_ASSETS_PUBLIC_URL` to control the returned asset URL prefix.
The value may be a relative path such as `/assets` or an absolute URL such as a
CDN/static-file origin. With the default value, the backend generates URLs of
the form:

```text
/assets/{packSlug}/{packVersion}/{variant}/{assetPath}
```

## Development

Start the backend (requires a populated PostgreSQL instance):

```bash
cd webnei
./gradlew bootRun
```

Start the frontend dev server:

```bash
cd webnei/ui
pnpm install
pnpm dev
```

The Vite dev server proxies `/api` to `http://127.0.0.1:8080`.

Verify the backend is healthy:

```bash
curl http://127.0.0.1:8080/api/dataset/list
```

Note: the frontend dev server only proxies `/api`. Asset URLs returned by the
backend must already be resolvable in your dev environment.

## Build

Backend build:

```bash
cd webnei
./gradlew build
```

Frontend production build:

```bash
cd webnei/ui
pnpm build
```

These are currently separate. The Gradle backend build does not run the Vite
build or copy `ui/dist` into Spring Boot resources automatically.

## Checks

| Command | What it does |
| --- | --- |
| `./gradlew compileJava` | Compile backend |
| `./gradlew build` | Build backend |
| `pnpm typecheck` | Type-check frontend |
| `pnpm lint` | Lint frontend |
| `pnpm build` | Production frontend build |
