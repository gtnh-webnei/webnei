# webnei

Language: [English](README.md) | [中文](README.zh_CN.md)

Spring Boot + JPA backend with a Vue 3 / Vite frontend for browsing NESQL
export data — items, fluids, mobs, recipes, quests, Thaumcraft aspects, and
GT ore/worldgen data.

## Architecture

```text
PostgreSQL (read-only)
  -> Spring Boot + JPA (read-model views & entities)
  -> JSON API (/api/**)
  -> Vue 3 + Vite SPA (pnpm)
```

- The backend is a **read-only query service** backed by JPA entities mapped
  to NESQL export tables and read-model views.
- The frontend is a separate Vite project in `ui/`.
- Static assets (rendered icons, textures) are **not** served by Spring Boot.
  In dev, the Vite middleware serves `/assets/**` from a local export directory.
  In prod, nginx or a static file server handles them.

## Requirements

- Java 21
- PostgreSQL with the `pg_trgm` extension (the NESQL schema requires it)
- Node.js 22+ with pnpm

## Configuration

All settings can be overridden via environment variables.

| Variable | Default | Purpose |
| --- | --- | --- |
| `WEBNEI_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/nesql` | NESQL database URL |
| `WEBNEI_DATASOURCE_USERNAME` | `postgres` | Database user |
| `WEBNEI_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `WEBNEI_ASSETS_PUBLIC_URL` | `/assets` | Public URL prefix for asset links |
| `WEBNEI_CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | CORS allowed origins |

The defaults assume a local PostgreSQL on `localhost:5432`. For development
against a remote database, either set the variables when running:

```bash
WEBNEI_DATASOURCE_URL='jdbc:postgresql://<host>:5432/nesql' \
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

| Controller | Endpoints |
| --- | --- |
| **Datasets** | `GET /api/datasets`, `/api/datasets/{id}`, `/api/datasets/{id}/mods/page` |
| **Items** | `GET .../items?q=&modId=&page=&size=`, `.../items/{variantId}`, `.../mods` |
| **Fluids** | `GET .../fluids?q=&modId=&page=&size=`, `.../fluids/{variantId}`, `.../fluid-mods` |
| **Mobs** | `GET .../mobs?q=&modId=&page=&size=`, `.../mobs/{variantId}`, `.../mobs/mods` |
| **Recipes** | `GET .../recipe-categories/page?q=&modId=&hideEmpty=`, `.../recipes/lookup?target=&kind=`, `.../recipes/lookup/breakdown`, `.../recipes/{id}`, `.../categories/{id}/recipes`, `.../categories/{id}/machines`, `.../categories/{id}/voltage-tiers` |
| **Quests** | `GET .../quest-lines`, `.../quest-lines/{id}`, `.../quests/{id}` |
| **Extras** | `GET .../items/{id}/extras`, `.../items/{id}/containers`, `.../fluids/{id}/extras` |
| **GT Ore** | `GET .../gt/ore-veins?q=&dimension=`, `.../gt/small-ores`, `.../gt/underground-fluids`, `.../gt/bartworks-ores`, plus detail endpoints |

## Asset Layout

Extract the NESQL export bundle into a directory pointed to by
`WEBNEI_ASSETS_DIR` (used by the Vite dev server):

```text
{packs}/
  {pack_slug}/{pack_version}/{variant}/{language}/
    item/...   (rendered item icons)
    mob/...    (entity portrait renders)
    spec/...   (display spec files)
    ...
```

The backend generates asset URLs of the form:

```text
/assets/{packSlug}/{packVersion}/{variant}/{language}/{assetPath}?v={sha256}
```

## Development

Start the backend (requires a populated PostgreSQL instance):

```bash
cd webnei
./gradlew bootRun
```

Start the frontend dev server (requires `WEBNEI_ASSETS_DIR`):

```bash
cd webnei/ui
pnpm install
WEBNEI_ASSETS_DIR=/path/to/exports pnpm dev
```

The Vite dev server proxies `/api` to `localhost:8080` and serves
`/assets/**` from the local export directory.

Verify the backend is healthy:

```bash
curl http://localhost:8080/api/datasets
```

## Build

```bash
./gradlew build
```

The Gradle build runs `vue-tsc` type-check, the Vite production build, and
copies `ui/dist` into Spring Boot static resources for the fat JAR.

## Checks

| Command | What it does |
| --- | --- |
| `./gradlew compileJava` | Compile backend |
| `pnpm typecheck` | Type-check frontend |
| `pnpm lint` | Lint frontend |
| `pnpm build` | Production frontend build |
