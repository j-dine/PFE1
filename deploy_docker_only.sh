#!/usr/bin/env bash
set -euo pipefail

# deploy_docker_only.sh
# Build images locally, stream them to the remote host's Docker daemon,
# then create network/volumes and run containers via `docker run` (no compose).
# Usage: ./deploy_docker_only.sh <remote_user> <remote_host> [ssh_port]

usage(){
  echo "Usage: $0 <remote_user> <remote_host> [ssh_port]"
  exit 2
}

if [[ ${#@} -lt 2 ]]; then
  usage
fi

REMOTE_USER="$1"
REMOTE_HOST="$2"
SSH_PORT="${3:-22}"

echo "Deploying to ${REMOTE_USER}@${REMOTE_HOST} via SSH port $SSH_PORT"

SERVICES=(
  discovery-service
  api-gateway
  user-service
  dossier-service
  workflow-service
  paiement-service
  notification-service
  document-service
  frontend
)

# Build each service image locally and stream to remote docker
for svc in "${SERVICES[@]}"; do
  if [[ -d "$svc" ]]; then
    img="pfe1-${svc}:latest"
    echo "Building image for $svc -> $img"
    docker build -t "$img" "$svc"
    echo "Streaming $img to remote..."
    docker save "$img" | ssh -p "$SSH_PORT" "$REMOTE_USER@$REMOTE_HOST" "docker load"
  else
    echo "Directory $svc not found, skipping build (if this is intentional use an external image)"
  fi
done

echo "Ensuring remote Docker network and volumes"
ssh -p "$SSH_PORT" "$REMOTE_USER@$REMOTE_HOST" bash -s <<'SSH_EOF'
set -euo pipefail
docker network inspect microservices-network >/dev/null 2>&1 || docker network create microservices-network
docker volume inspect pfe1_postgres_data >/dev/null 2>&1 || docker volume create pfe1_postgres_data
docker volume inspect pfe1_grafana_data >/dev/null 2>&1 || docker volume create pfe1_grafana_data
docker volume inspect pfe1_document_uploads >/dev/null 2>&1 || docker volume create pfe1_document_uploads
SSH_EOF

echo "Start core containers on remote"
# Postgres
ssh -p "$SSH_PORT" "$REMOTE_USER@$REMOTE_HOST" bash -s <<'SSH_EOF'
set -euo pipefail
docker rm -f pfe1-postgres >/dev/null 2>&1 || true
docker run -d --name pfe1-postgres \
  --network microservices-network \
  -e POSTGRES_DB=workflowdb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres \
  -v pfe1_postgres_data:/var/lib/postgresql/data \
  -p 15432:5432 \
  postgres:15
SSH_EOF

# Discovery (Eureka) - uses the built image pfe1-discovery-service
ssh -p "$SSH_PORT" "$REMOTE_USER@$REMOTE_HOST" bash -s <<'SSH_EOF'
set -euo pipefail
docker rm -f pfe1-discovery-service >/dev/null 2>&1 || true
docker run -d --name pfe1-discovery-service \
  --network microservices-network \
  -p 8761:8761 \
  pfe1-discovery-service:latest
SSH_EOF

# API Gateway
ssh -p "$SSH_PORT" "$REMOTE_USER@$REMOTE_HOST" bash -s <<'SSH_EOF'
set -euo pipefail
docker rm -f pfe1-api-gateway >/dev/null 2>&1 || true
docker run -d --name pfe1-api-gateway \
  --network microservices-network \
  -p 8080:8080 \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka/ \
  -e JWT_SECRET="my-super-secret-key-for-jwt-signing-32chars-min" \
  pfe1-api-gateway:latest
SSH_EOF

# User service
ssh -p "$SSH_PORT" "$REMOTE_USER@$REMOTE_HOST" bash -s <<'SSH_EOF'
set -euo pipefail
docker rm -f pfe1-user-service >/dev/null 2>&1 || true
docker run -d --name pfe1-user-service \
  --network microservices-network \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://pfe1-postgres:5432/workflowdb \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=postgres \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-service:8761/eureka/ \
  -e APP_BOOTSTRAP_ADMIN_ENABLED=true \
  -e APP_BOOTSTRAP_ADMIN_USERNAME=admin \
  -e APP_BOOTSTRAP_ADMIN_PASSWORD=Admin@123 \
  -e APP_BOOTSTRAP_ADMIN_EMAIL=admin@bo.com \
  pfe1-user-service:latest
SSH_EOF

# Other microservices (start them similarly) - run in background without host ports
for svc in dossier-service workflow-service paiement-service notification-service document-service; do
  img="pfe1-${svc}:latest"
  echo "Starting ${svc} on remote"
  ssh -p "$SSH_PORT" "$REMOTE_USER@$REMOTE_HOST" bash -s <<SSH_EOF
set -euo pipefail
docker rm -f pfe1-${svc} >/dev/null 2>&1 || true
docker run -d --name pfe1-${svc} --network microservices-network $img
SSH_EOF
done

# Frontend (nginx)
ssh -p "$SSH_PORT" "$REMOTE_USER@$REMOTE_HOST" bash -s <<'SSH_EOF'
set -euo pipefail
docker rm -f pfe1-frontend >/dev/null 2>&1 || true
docker run -d --name pfe1-frontend \
  --network microservices-network \
  -p 80:80 \
  pfe1-frontend:latest
SSH_EOF

echo "Deployment finished. Check remote containers:"
ssh -p "$SSH_PORT" "$REMOTE_USER@$REMOTE_HOST" "docker ps --filter name=pfe1- --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'"

echo "Notes:"
echo " - This script launches containers with minimal env derived from docker-compose.yml. Adjust env/volumes as needed."
echo " - If you prefer persistent DB reset, remove the postgres volume on remote: docker volume rm pfe1_postgres_data"
