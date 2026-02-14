#!/bin/bash

echo "======================================"
echo "  启动 Ollama Chat Demo"
echo "======================================"
echo ""

# 检查 Ollama 是否运行
echo "检查 Ollama 服务状态..."
if ! curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
    echo "❌ Ollama 服务未运行！"
    echo "请先启动 Ollama 服务："
    echo "  OLLAMA_HOST=0.0.0.0 ollama serve"
    exit 1
fi

echo "✅ Ollama 服务运行正常"
echo ""

# 检查模型是否存在
echo "检查 deepseek-r1:7b 模型..."
if ! curl -s http://localhost:11434/api/tags | grep -q "deepseek-r1:7b"; then
    echo "❌ 模型 deepseek-r1:7b 未找到！"
    echo "请先拉取模型："
    echo "  ollama pull deepseek-r1:7b"
    exit 1
fi

echo "✅ 模型已就绪"
echo ""

# 启动 Spring Boot 应用
echo "启动 Spring Boot 应用..."
echo ""

if [ ! -f "pom.xml" ]; then
    echo "❌ 未找到 pom.xml 文件，请确保在项目根目录运行此脚本"
    exit 1
fi

# 使用 Maven 启动
./mvnw spring-boot:run

# 如果没有 mvnw，使用系统的 maven
if [ $? -ne 0 ]; then
    echo "尝试使用系统 Maven..."
    mvn spring-boot:run
fi
