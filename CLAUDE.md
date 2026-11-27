# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Layout

- `api/` — Spring Boot 4 / Kotlin / Gradle (Kotlin DSL) REST API, Java 21. Package `com.example.api`, layered: `config/`, `controller/` (HTTP only, delegates to services), `service/`, `repository/`, `model/entity/`, `model/dto/`, `mapper/` (entity→DTO extension functions), `exception/` (custom exceptions + `GlobalExceptionHandler` returning `ProblemDetail`). Tests mirror this; shared helpers live in `support/`.
- `web/` — React Router **v7** (pinned `^7`; `create-react-router@latest` now scaffolds v8) in framework mode, **SPA mode (`ssr: false`), no server-side rendering**: `npm run build` emits static files in `build/client/`, and there is no Node server at runtime. Stack: Vite 8, Tailwind 4, TypeScript (`~/*` → `app/*`). Routes are configured in `app/routes.ts`. Global state uses Redux Toolkit in `app/store/`, provided by `StoreProvider` in `root.tsx`. Use the typed `useAppSelector`/`useAppDispatch` from `store/hooks.ts`.
- `.devcontainer/` — Podman-based devcontainer: `app` (Java 21 and Node 24 from `mise.toml`, AWS CLI, psql), `db` (Postgres 19), `db-test` (Postgres 19 on tmpfs), `aws-emulator` (AWS Emulator, built from `.devcontainer/emulator/Dockerfile`).
- `.local/bin/` — helper scripts, put on `PATH` by direnv; `.local/lib/common.sh` holds shared helpers.
- `mise.toml` — pins node, java (Microsoft 21.0.13.1), gradle (9.9.2 = wrapper version), lefthook. Entering the repo with direnv runs `mise install` and `lefthook install`. mise is required by the scripts.

## Commands

Everything runs through Podman; use the scripts, not `docker`/`podman compose` directly.

- `devc-build` / `devc-up` (start + open shell) / `devc-enter [cmd…]` / `devc-stop` / `devc-down [-y]` (destroys volumes incl. Postgres data).
- `format-code [--check]` — Spotless + ktlint for `api/`. Also runs as the lefthook pre-commit hook (re-stages fixes). Just runs `./gradlew spotlessApply`/`spotlessCheck` in `api/` with whatever Java is on the PATH.
- Build/test/run happen **inside the container** (`devc-enter`), from `api/`:
  - `./gradlew test` — all tests; single class: `./gradlew test --tests '*ItemControllerTests'`; single test: `--tests '*AuthControllerTests.login returns a bearer token*'`
  - `./gradlew spotlessCheck` / `spotlessApply`
  - `./gradlew bootRun` — serves :8080; Swagger UI at `/swagger-ui.html`, spec at `/v3/api-docs`.
- From `web/`: `npm run dev` (:5173, forwarded), `npm run typecheck`, `npm run build`, `npm start` (previews the static `build/client/`). Both proxy `/api/*` to `localhost:8080` (`web/vite.config.ts`) and pass the browser `Host` through so HAL links point back at the dev server, so the web app calls relative `/api/...` URLs and needs no CORS.

## Architecture and gotchas

**Networking.** `aws-emulator` joins the `app` container's network namespace (`network_mode: service:app`), so it is at `https://127.0.0.1:9297` (required for S3 path-style and the cert). Consequently `app` must NOT `depends_on` it (cycle), and `devcontainer up --remove-existing-container` fails — use `compose down` then `devc-up`. **`devc-up` does not pick up `compose.yaml` changes**: run `source .local/lib/common.sh; compose down` first, then `devc-up`.

**AWS/Java trust.** The emulator makes a new CA/credentials each start (shared via the `emulator-data` volume). `postStartCommand` (`.devcontainer/scripts/emulator-trust.sh`) waits for it and builds a merged JDK truststore (default CAs + emulator CA) referenced by `JAVA_TOOL_OPTIONS`. AWS env vars come from `compose.yaml`.

**Two databases.** Dev app uses `db:5432/app` (persistent). Tests use the `test` Spring profile → `db-test:5432/app_test` (tmpfs, lost on stop). Do not set `SPRING_DATASOURCE_*` env vars in compose: env outranks properties files and would send tests to the dev DB.

**Test data model.** Tests (`@IntegrationTest` meta-annotation in `api/src/test`) commit their data so it can be inspected afterwards (`psql -h db-test -d app_test`). `TestDatabaseReset` truncates all tables (except `databasechangelog`/`databasechangeloglock`) once per JVM at startup. Hence tests must not assume empty tables. `TestUsers` get-or-creates a user and logs in to get a bearer header. Test task is never up-to-date (depends on external DB state).

**Schema.** Liquibase changelogs (`db/changelog/db.changelog-master.xml` including SQL changesets in `changes/`) in `api/src/main/resources` own the schema; Hibernate is `ddl-auto=validate`. Editing an already-applied changeset breaks the checksum on persistent DBs (the dev `db`); the tmpfs test DB is unaffected after a restart.

**Security.** Stateless JWT (HS256, Spring resource server), all endpoints require a token except `POST /api/auth/login`, `/actuator/health`, and OpenAPI/Swagger. Passwords: Argon2id via `DelegatingPasswordEncoder` (needs BouncyCastle). Users in `app_user`; username uniqueness is a partial unique index on `lower(username) WHERE enabled`, so login must use `AppUserRepository.findActiveByUsername` (Spring's `IgnoreCase` derives `upper(...)` and won't use the index). The app refuses to start without `JWT_SECRET` (≥32 bytes); compose provides dev-only values plus an `admin` bootstrap user (`BootstrapUserRunner`, skipped under the `test` profile).

**Paging and HATEOAS.** Every endpoint that returns a list of items must be paged, never an unbounded list. Lists use cursor (keyset) pagination, not offset: `?cursor=<id>` (items after it) or `?before=<id>` (items before it, still returned oldest first), plus `size` (default 20, max 100), ordered by UUIDv7 `id`; the service fetches `size + 1` rows to detect more items (see `ItemService.list`). Responses are HAL (Spring HATEOAS): collections are `CollectionModel` with `self`, `first` and, when more exist, `prev` and `next` links; single items are `EntityModel` built by a `*ModelAssembler` with `self` and `collection` links. Clients follow links instead of building URLs.

**Config style.** Use `application.properties`, not YAML.

**Formatting.** `api/.editorconfig` sets ktlint_official, 4-space indent. When scripting edits to `build.gradle.kts`, note Spotless converts it to spaces (not tabs).

**Initializr quirk.** Don't pass `bootVersion` to start.spring.io (it 500s); omit it for the default.
