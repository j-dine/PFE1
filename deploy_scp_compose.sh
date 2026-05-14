#!/usr/bin/env bash
set -euo pipefail

# deploy_scp_compose.sh
# Copy project to remote server then run `docker compose up --build -d` there.
# Usage: ./deploy_scp_compose.sh <remote_user> <remote_host> [ssh_port] [remote_dir] [keep_volumes]

usage(){
  echo "Usage: $0 <remote_user> <remote_host> [ssh_port] [remote_dir] [keep_volumes]"
  echo "  remote_dir default: /root/PF1"
  echo "  keep_volumes: if 'true' will not remove volumes when bringing containers down"
  exit 2
}

if [[ ${#@} -lt 2 ]]; then
  usage
fi

REMOTE_USER="$1"
REMOTE_HOST="$2"
SSH_PORT="${3:-22}"
REMOTE_DIR="${4:-/root/PF1}"
KEEP_VOLUMES="${5:-false}"

echo "Deploying to ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR} via SSH port ${SSH_PORT}"

# Exclude unnecessary files
EXCLUDES=(--exclude='.git' --exclude='node_modules' --exclude='**/node_modules' --exclude='.idea' --exclude='.vscode' --exclude='*.iml' --exclude='.m2')

echo "Preparing files to copy..."

echo "Skipping local Maven/npm builds. Remote docker compose will build all images in containers."

echo "Creating archive and streaming to remote..."
# Use tar over SSH to copy workspace reliably (preserves permissions and avoids scp recursion issues)
tar "${EXCLUDES[@]}" -czf - . | ssh -p "$SSH_PORT" "$REMOTE_USER@$REMOTE_HOST" "mkdir -p '$REMOTE_DIR' && tar -xzf - -C '$REMOTE_DIR'"

echo "Files copied to ${REMOTE_HOST}:${REMOTE_DIR}"

echo "Running docker compose on remote"
ssh -p "$SSH_PORT" "$REMOTE_USER@$REMOTE_HOST" "REMOTE_DIR='$REMOTE_DIR' KEEP_VOLUMES='$KEEP_VOLUMES' bash -s" <<'SSH_EOF'
set -euo pipefail

# Check Docker
if ! command -v docker >/dev/null 2>&1; then
  echo "Error: Docker not found on remote host" >&2
  exit 3
fi

# Choose compose command
compose_cmd='docker compose'
if ! docker compose version >/dev/null 2>&1; then
  if command -v docker-compose >/dev/null 2>&1; then
    compose_cmd='docker-compose'
  else
    echo "Error: docker compose or docker-compose not available on remote" >&2
    exit 4
  fi
fi

cd "$REMOTE_DIR"

if [ "${KEEP_VOLUMES}" = "true" ]; then
  $compose_cmd down || true
else
  $compose_cmd down -v || true
fi

$compose_cmd up --build -d

$compose_cmd ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
SSH_EOF

echo "Remote docker compose completed. Use 'ssh -p $SSH_PORT $REMOTE_USER@$REMOTE_HOST "docker ps"' to inspect containers."

echo "Done."
