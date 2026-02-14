@echo off
echo ======================================
echo   启动 Ollama Chat Demo
echo ======================================
echo.

REM 检查 Ollama 是否运行
echo 检查 Ollama 服务状态...
curl -s http://localhost:11434/api/tags >nul 2>&1
if errorlevel 1 (
    echo ❌ Ollama 服务未运行！
    echo 请先启动 Ollama 服务：
    echo   set OLLAMA_HOST=0.0.0.0
    echo   ollama serve
    exit /b 1
)

echo ✅ Ollama 服务运行正常
echo.

REM 启动 Spring Boot 应用
echo 启动 Spring Boot 应用...
echo.

if not exist "pom.xml" (
    echo ❌ 未找到 pom.xml 文件，请确保在项目根目录运行此脚本
    exit /b 1
)

REM 使用 Maven 启动
mvnw.cmd spring-boot:run

if errorlevel 1 (
    echo 尝试使用系统 Maven...
    mvn spring-boot:run
)
