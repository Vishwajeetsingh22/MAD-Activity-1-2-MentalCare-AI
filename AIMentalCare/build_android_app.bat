@echo off
echo ===================================================
echo   Building AI MentalCare Android Application...
echo ===================================================
cd /d "%~dp0AIMentalCareApp"
call gradlew.bat assembleDebug
echo.
echo APK build complete! Output path:
echo %~dp0AIMentalCareApp\app\build\outputs\apk\debug\app-debug.apk
pause
