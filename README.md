# webnei

Spring Boot MVC + JDBC API with a Vite/Vue frontend for browsing imported NESQL export data.

## Runtime

- PostgreSQL data is imported outside this app.
- Spring Boot reads the NESQL schema directly through JDBC.
- Frontend assets are served by Spring Boot under `/assets/**`.
- The Vite dev server proxies `/api` and `/assets` to Spring Boot.

## Configuration

Defaults are intentionally portable and can be overridden by environment variables.

| Variable | Default | Purpose |
| --- | --- | --- |
| `WEBNEI_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/nesql` | NESQL PostgreSQL connection URL |
| `WEBNEI_DATASOURCE_USERNAME` | `postgres` | database user |
| `WEBNEI_DATASOURCE_PASSWORD` | `postgres` | database password |
| `WEBNEI_ASSETS_ROOT` | `assets` | extracted asset bundle root |

## Asset Layout

Extract each export asset bundle into:

```text
assets/{pack_slug}/{pack_version}/{variant}/{language}/...
```

The backend maps `asset.path` to that relative directory and returns cache-busted URLs:

```text
/assets/{packSlug}/{packVersion}/{variant}/{language}/{asset.path}?v={sha256}
```

## API

First pass endpoints are read-only:

- `GET /api/datasets`
- `GET /api/datasets/{datasetId}/items`
- `GET /api/datasets/{datasetId}/nei/items`
- `GET /api/datasets/{datasetId}/recipe-categories`
- `GET /api/datasets/{datasetId}/recipes/{recipeId}`
- `GET /api/datasets/{datasetId}/recipes/lookup`
- `GET /api/datasets/{datasetId}/assets/{assetId}`

## Development

```bash
./gradlew bootRun
npm --prefix ui run dev
```

## Build

```bash
./gradlew build
```

The Gradle build runs the Vue production build and copies `ui/dist` into Spring Boot static resources.
