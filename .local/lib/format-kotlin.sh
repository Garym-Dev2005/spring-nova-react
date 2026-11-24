#!/usr/bin/env bash
# pre-commit: format the api/ Kotlin sources with Spotless (ktlint).
# Prefers the running dev container (it has the pinned JDK/Gradle); falls back
# to a host JDK; otherwise fails so unformatted code isn't committed silently.
# Usage: format-kotlin.sh [--check]
source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

task=spotlessApply
[[ "${1:-}" == "--check" ]] && task=spotlessCheck

if [[ -n "$(app_container)" ]]; then
  "$ROOT/.local/bin/devc-enter" bash -c "cd api && ./gradlew $task --console=plain -q"
elif command -v java >/dev/null 2>&1; then
  (cd "$ROOT/api" && ./gradlew "$task" --console=plain -q)
else
  die "can't format Kotlin: dev container isn't running and no host JDK found. Run devc-up (or 'git commit --no-verify' to skip)."
fi
