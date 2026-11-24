# spring-nova-react

A Spring Boot 4 / Kotlin REST API (`api/`) developed inside a Podman-based devcontainer. A React front end is planned but doesn't exist yet.

## What's in the box

| Path | Purpose |
| --- | --- |
| `api/` | Spring Boot 4 + Kotlin + Gradle (Kotlin DSL), Java 21, package `com.example.api` |
| `.devcontainer/` | Compose setup: `app` (Java 21, Node 24, AWS CLI, psql), `db` (Postgres 19), `db-test` (Postgres 19 on tmpfs), `aws-emulator` |
| `.local/bin/` | Helper scripts (`devc-*`, `format-code`), put on your `PATH` by direnv |
| `mise.toml` | Pinned tool versions: node, java, gradle, lefthook |

## Prerequisites

- [Podman](https://podman.io) with a compose provider (`podman compose`). On macOS, start a Podman machine first.
- [mise](https://mise.jdx.dev). The helper scripts require it.
- [direnv](https://direnv.net), recommended. Entering the repo then runs `mise install`, installs the git hooks (lefthook) and adds `.local/bin` to `PATH`. Without direnv, add `.local/bin` to `PATH` yourself and run `mise install` and `lefthook install` once.

Run `direnv allow` once after cloning.

## Quick start

```sh
devc-build     # build the images (first time, or after changing the Dockerfile)
devc-up        # start app + databases + AWS emulator, and open a shell in the app container
```

Then, inside the container, from `api/`:

```sh
cd api
./gradlew bootRun    # serves http://localhost:8080
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI spec: http://localhost:8080/v3/api-docs
- Health: http://localhost:8080/actuator/health

On first run the app creates an `admin` user. The dev password comes from `.devcontainer/compose.yaml`. Get a token with `POST /api/auth/login`, then send it as `Authorization: Bearer <token>`. Every other endpoint needs a token.

## Helper scripts

Use these, not `docker` or `podman compose` directly.

| Command | What it does |
| --- | --- |
| `devc-build` | Build the dev container images |
| `devc-up` | Start the containers and open a shell |
| `devc-enter [cmd…]` | Open a shell, or run a command, in the running container |
| `devc-stop` | Stop the containers and keep the Postgres data |
| `devc-down [-y]` | Remove containers, images **and volumes, including Postgres data** |
| `format-code [--check]` | Format `api/` with Spotless and ktlint. Also runs as the pre-commit hook |

## Building and testing

Run these inside the container (`devc-enter`), from `api/`:

```sh
./gradlew test                                             # all tests
./gradlew test --tests '*ItemControllerTests'              # one class
./gradlew test --tests '*AuthControllerTests.login returns a bearer token*'   # one test
./gradlew spotlessCheck   # or spotlessApply
```

Tests use the `test` Spring profile and a separate database, so they never touch your dev data:

| | Dev app | Tests |
| --- | --- | --- |
| Host | `db:5432` | `db-test:5432` |
| Database | `app` (persistent) | `app_test` (tmpfs, lost on stop) |

Tests commit their data so you can inspect it afterwards with `psql -h db-test -d app_test`. All tables are truncated once per JVM start, so tests must not assume empty tables.

## Configuration notes

- **Schema** is owned by Liquibase changelogs in `api/src/main/resources/db/changelog`. Hibernate only validates. Don't edit a changeset that has already been applied to your dev database, because that breaks its checksum.
- **Security** is stateless JWT (HS256). The app won't start without `JWT_SECRET` (at least 32 bytes). Compose supplies a dev-only value.
- **Config files** are `application.properties`, not YAML.
- **AWS emulator** is at `https://127.0.0.1:9297` inside the container. It generates a new CA and credentials on each start, and the container trusts them automatically.
- **Changing `compose.yaml`:** `devc-up` doesn't pick up changes. Run `source .local/lib/common.sh; compose down`, then `devc-up`.

## Troubleshooting

**`error listing credentials ... docker-credential-desktop: executable file not found`**
`~/.docker/config.json` has `"credsStore": "desktop"` left over from Docker Desktop. Remove that line, or point `DOCKER_CONFIG` at a directory with an empty `config.json`.
