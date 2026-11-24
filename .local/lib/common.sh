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

# Run a command under the Node version pinned in .nvmrc (via mise, fnm or nvm
# if the active node doesn't match).
run_node() {
  local want have
  want="$(tr -d 'v \r\n' < "$ROOT/.nvmrc")"
  have="$(node -v 2>/dev/null | sed 's/^v//' || true)"
  if [[ -n "$have" && "${have%%.*}" == "${want%%.*}" ]]; then
    "$@"
  elif command -v mise >/dev/null; then
    mise exec "node@$want" -- "$@"
  elif command -v fnm >/dev/null; then
    fnm exec --using="$want" "$@"
  elif [[ -s "${NVM_DIR:-$HOME/.nvm}/nvm.sh" ]]; then
    # shellcheck disable=SC1091
    source "${NVM_DIR:-$HOME/.nvm}/nvm.sh"
    nvm install "$want" >/dev/null && nvm use "$want" >/dev/null
    "$@"
  else
    die "Node $want (from .nvmrc) required; found '${have:-none}'. Install mise, fnm or nvm."
  fi
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
