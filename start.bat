@echo off
REM Commonplace Book — clica e sobe tudo (API + Postgres no Docker, desktop fora)

echo [1/3] Subindo API + Postgres (Docker)...
docker compose up --build -d
if errorlevel 1 (
  echo Falha ao subir o Docker Compose. O Docker Desktop esta rodando?
  exit /b 1
)

echo [2/3] Esperando a API responder em http://localhost:8080 ...
:wait_api
curl -s -o nul http://localhost:8080
if errorlevel 1 (
  timeout /t 2 /nobreak >nul
  goto wait_api
)
echo API no ar.

echo [3/3] Abrindo o desktop (Electron)...
cd desktop
if not exist node_modules (
  echo Instalando dependencias do desktop (primeira vez)...
  call npm install
)
call npm run dev
