#!/bin/bash
# ==========================================
# QA Agent RAG Platform - Demo Launcher
# ==========================================

echo "Starting QA Agent RAG Platform..."
echo ""

# Check Docker
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is required but not installed."
    exit 1
fi

# Start PostgreSQL + PGVector
echo "[1/3] Starting PostgreSQL with PGVector..."
docker compose up -d pgvector

echo "Waiting for PostgreSQL to be ready..."
until docker compose exec pgvector pg_isready -U user -d qaagent 2>/dev/null; do
    sleep 2
done
echo "PostgreSQL is ready!"

# Copy config
echo "[2/3] Setting up application config..."
if [ ! -f "src/main/resources/application.yml" ]; then
    cp src/main/resources/application-example.yml src/main/resources/application.yml
    echo "  Created application.yml from example."
    echo "  Edit src/main/resources/application.yml to set your AI_API_KEY"
fi

# Build and run
echo "[3/3] Building and starting Spring Boot..."
./mvnw spring-boot:run

echo ""
echo "Application started at http://localhost:8080"
echo "Try: curl http://localhost:8080/api/health"
