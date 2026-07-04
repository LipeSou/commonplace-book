#!/usr/bin/env bash
# Commonplace Book — clica e sobe tudo (API + Postgres no Docker, desktop fora)
set -e

echo "[1/3] Subindo API + Postgres (Docker)..."
docker compose up --build -d

echo "[2/3] Esperando a API responder em http://localhost:8080 ..."
until curl -s -o /dev/null http://localhost:8080; do
  sleep 2
done
echo "API no ar."

echo "[3/3] Abrindo o desktop (Electron)..."
cd desktop
if [ ! -d node_modules ]; then
  echo "Instalando dependências do desktop (primeira vez)..."
  npm install
fi
npm run dev
