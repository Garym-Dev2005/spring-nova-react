FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04

# Postgres client tools (PGDG repo; falls back to the distro client).
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get install -y --no-install-recommends postgresql-common ca-certificates \
    && /usr/share/postgresql-common/pgdg/apt.postgresql.org.sh -y \
    && (apt-get install -y --no-install-recommends postgresql-client-19 \
        || apt-get install -y --no-install-recommends postgresql-client) \
    && rm -rf /var/lib/apt/lists/*

# Pre-create the shared mount point with the right owner.
USER vscode
RUN mkdir -p /home/vscode/.aws/emulator /home/vscode/.aws/emulator-trust
