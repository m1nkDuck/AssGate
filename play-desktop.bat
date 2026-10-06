@echo off
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0tools\launch.ps1" -Mode desktop
if errorlevel 1 echo Could not start AshGate. Read the error above.
pause
