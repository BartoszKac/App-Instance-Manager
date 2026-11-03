@echo off
cd /d "%~dp0"
docker compose up -d
if errorlevel 1 (
  echo Docker nie wystartowal. Uruchom Docker Desktop i sprobuj ponownie.
  pause
  exit /b 1
)
start "" powershell -NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File "%~dp0lan-helper\lan-helper.ps1"
start "" http://localhost:5173
