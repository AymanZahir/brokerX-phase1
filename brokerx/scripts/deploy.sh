#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
PROJECT_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)

pushd "$PROJECT_ROOT" >/dev/null

echo "[brokerx] Building Docker image..."
docker compose build --pull api1

echo "[brokerx] Restarting stack..."
docker compose up -d --remove-orphans

echo "[brokerx] Stack running. Check logs with: docker compose logs -f"

echo "[brokerx] To rollback: docker compose down && docker compose up -d api1 api2 gateway db"

popd >/dev/null
