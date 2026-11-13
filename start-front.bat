@echo off
chcp 65001 >nul
cd /d D:\DSH\camp-rent\camp-front
echo Starting Vite dev server...
npm run dev > D:\DSH\camp-rent\front.log 2>&1
echo.
echo ===== frontend exited, code=%errorlevel% =====
pause
