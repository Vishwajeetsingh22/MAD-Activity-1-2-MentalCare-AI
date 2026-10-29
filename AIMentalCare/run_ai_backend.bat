@echo off
echo ===================================================
echo   Starting AI MentalCare Python Backend Server...
echo ===================================================
cd /d "%~dp0backend_server"
echo Installing/Verifying Python dependencies...
py -m pip install -r requirements.txt
echo Starting Flask Server...
py app.py
pause
