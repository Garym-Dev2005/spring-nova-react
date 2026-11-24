# Shared helpers for the devc-* scripts. Source this; don't execute it.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
COMPOSE_FILE="$ROOT/.devcontainer/compose.yaml"
DEFAULT_PROJECT="react-java-dev"
DEVCONTAINERS_CLI_VERSION="0.91.0"

# compose.yaml mounts the workspace using this variable.
export LOCAL_WORKSPACE_FOLDER_BASENAME="$(basename "$ROOT")"

die() { echo "error: $*" >&2; exit 1; }

command -v podman >/dev/null || die "podman is not installed"

# Run a command under the Node version pinned in mise.toml (installed on demand).
run_node() {
  command -v mise >/dev/null || die "mise is required (it pins Node in mise.toml): https://mise.jdx.dev"
  mise exec -C "$ROOT" -- "$@"
}

# devcontainer CLI, pinned, pointed at Podman.
dc() {
  local sub="$1"; shift
  run_node npx --yes "@devcontainers/cli@$DEVCONTAINERS_CLI_VERSION" "$sub" \
    --workspace-folder "$ROOT" \
    --docker-path podman \
    --docker-compose-path "$ROOT/.local/lib/podman-compose" "$@"
}

# Container IDs (running only by default; pass -a for all) of the dev container.
app_container() {
  podman ps "$@" -q \
    --filter "label=devcontainer.local_folder=$ROOT" \
    --filter "label=com.docker.compose.service=app" | head -1
}

# Compose project name actually used by the devcontainer CLI for this folder.
compose_project() {
  local p
  p="$(podman ps -a --filter "label=devcontainer.local_folder=$ROOT" \
        --format '{{ index .Labels "com.docker.compose.project" }}' | head -1)"
  echo "${p:-$DEFAULT_PROJECT}"
}

compose() { podman compose -p "$(compose_project)" -f "$COMPOSE_FILE" "$@"; }
