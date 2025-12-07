@echo off
echo ========================================
echo Building Jewellery Microservices
echo ========================================

echo.
echo Step 1: Building all Maven modules...
call mvn clean package -DskipTests

if %ERRORLEVEL% neq 0 (
    echo Build failed! Exiting...
    exit /b 1
)

echo.
echo Step 2: Building Docker images...
docker-compose build

if %ERRORLEVEL% neq 0 (
    echo Docker build failed! Exiting...
    exit /b 1
)

echo.
echo ========================================
echo Build Complete!
echo ========================================
echo.
echo To start all services, run:
echo   docker-compose up -d
echo.
echo To view logs:
echo   docker-compose logs -f
echo.
echo To stop all services:
echo   docker-compose down
echo.
