FROM mcr.microsoft.com/devcontainers/base:ubuntu-24.04

# Postgres client tools (PGDG repo; falls back to the distro client).
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get install -y --no-install-recommends ripgrep curl bat postgresql-common ca-certificates \
    && /usr/share/postgresql-common/pgdg/apt.postgresql.org.sh -y \
    && (apt-get install -y --no-install-recommends postgresql-client-19 \
        || apt-get install -y --no-install-recommends postgresql-client) \
    && rm -rf /var/lib/apt/lists/*

# mise (official apt repo): installs the tools pinned in mise.toml.
RUN install -dm 755 /etc/apt/keyrings \
    && curl -fSs https://mise.jdx.dev/gpg-key.pub -o /etc/apt/keyrings/mise-archive-keyring.asc \
    && echo "deb [signed-by=/etc/apt/keyrings/mise-archive-keyring.asc] https://mise.jdx.dev/deb stable main" > /etc/apt/sources.list.d/mise.list \
    && apt-get update && apt-get install -y --no-install-recommends mise \
    && rm -rf /var/lib/apt/lists/*

# Pre-create the shared mount point with the right owner.
USER vscode
RUN mkdir -p /home/vscode/.aws/emulator /home/vscode/.aws/emulator-trust

# mise tool cache (a named volume is mounted here; pre-create it with the right owner).
RUN mkdir -p /home/vscode/.local/share/mise \
    && echo 'eval "$(mise activate zsh)"' >> /home/vscode/.zshrc \
    && echo 'eval "$(mise activate bash)"' >> /home/vscode/.bashrc
