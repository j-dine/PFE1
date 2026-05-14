#!/usr/bin/env bash
set -euo pipefail

# deploy.sh: build locally, rsync project to remote, and run docker compose there.
# Usage: ./deploy.sh <remote_user> <remote_host> [remote_dir] [ssh_port]
# Example: ./deploy.sh root 85.9.201.48 /root/TakaraEE 22

usage(){
  echo "Usage: $0 <remote_user> <remote_host> [remote_dir] [ssh_port]"
  echo "Example: $0 root 85.9.201.48 /root/PF1 22"
  exit 2
}

if [[ ${#@} -lt 2 ]]; then
  usage
fi

REMOTE_USER="$1"
REMOTE_HOST="$2"
REMOTE_DIR="${3:-/root/PF1}"
SSH_PORT="${4:-22}"
LOCAL_DIR="$(pwd)"

echo "Deploy settings:"
echo "  Local dir: $LOCAL_DIR"
echo "  Remote: ${REMOTE_USER}@${REMOTE_HOST}:${REMOTE_DIR} (ssh port $SSH_PORT)"

# Ensure required local commands exist
for cmd in rsync ssh; do
  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Required command not found: $cmd" >&2
    exit 1
  fi
done

# 1) Rsync project to remote
# Exclude large/dev-only folders. Adjust excludes if you want to copy target jars only.
RSYNC_EXCLUDES=(
  --exclude '.git'
  --exclude 'node_modules'
  --exclude 'frontend/node_modules'
  --exclude '.idea'
  --exclude '*.iml'
)

echo "Syncing files to remote..."
rsync -avz --delete "${RSYNC_EXCLUDES[@]}" "$LOCAL_DIR/" -e "ssh -p $SSH_PORT" "${REMOTE_USER}@${REMOTE_HOST}:$REMOTE_DIR"

# 2) Remote compose deploy
echo "Running docker compose on remote host..."
ssh -p "$SSH_PORT" "${REMOTE_USER}@${REMOTE_HOST}" \
  "mkdir -p '$REMOTE_DIR' && cd '$REMOTE_DIR' && \
   if ! command -v docker >/dev/null 2>&1; then echo 'Docker not found on remote'; exit 1; fi && \
   docker compose down -v || true && \
   docker compose pull || true && \
   docker compose up --build -d"

# 3) Post-deploy checks
echo "Post-deploy: listing containers (remote)"
ssh -p "$SSH_PORT" "${REMOTE_USER}@${REMOTE_HOST}" "cd '$REMOTE_DIR' && docker compose ps"

echo "Tail frontend logs (remote, last 200 lines):"
ssh -p "$SSH_PORT" "${REMOTE_USER}@${REMOTE_HOST}" "cd '$REMOTE_DIR' && docker compose logs --tail 200 --no-color frontend || true"

echo "Deploy finished. Check your services at http://$REMOTE_HOST/ and other configured ports."
