#!/bin/bash

# TechTorque Vehicle Service - Quick Start Script
# This script helps you quickly start the Vehicle Service

echo "🚗 TechTorque Vehicle Service - Quick Start"
echo "==========================================="
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

echo "✅ Java found: $(java -version 2>&1 | head -n 1)"
echo ""

# Check if PostgreSQL is running
echo "🔍 Checking PostgreSQL connection..."
if ! command -v psql &> /dev/null; then
    echo "⚠️  PostgreSQL client not found. Make sure PostgreSQL is running."
else
    echo "✅ PostgreSQL client found"
fi
echo ""

# Set environment variables
echo "🔧 Setting environment variables..."
export DB_HOST=${DB_HOST:-localhost}
export DB_PORT=${DB_PORT:-5432}
export DB_NAME=${DB_NAME:-techtorque_vehicles}
export DB_USER=${DB_USER:-techtorque}
export DB_PASS=${DB_PASS:-techtorque123}
export DB_MODE=${DB_MODE:-update}
export SPRING_PROFILE=${SPRING_PROFILE:-dev}
export UPLOAD_DIR=${UPLOAD_DIR:-uploads/vehicle-photos}

echo "  DB_HOST: $DB_HOST"
echo "  DB_PORT: $DB_PORT"
echo "  DB_NAME: $DB_NAME"
echo "  DB_USER: $DB_USER"
echo "  SPRING_PROFILE: $SPRING_PROFILE"
echo ""

# Create upload directory
echo "📁 Creating upload directory..."
mkdir -p "$UPLOAD_DIR"
echo "  Created: $UPLOAD_DIR"
echo ""

# Navigate to service directory
cd "$(dirname "$0")/vehicle-service" || exit

# Clean and build
echo "🔨 Building the service..."
./mvnw clean compile

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "🚀 Starting Vehicle Service..."
    echo ""
    echo "Service will be available at:"
    echo "  - Service: http://localhost:8082"
    echo "  - Swagger UI: http://localhost:8082/swagger-ui/index.html"
    echo "  - Health Check: http://localhost:8082/actuator/health"
    echo "  - API (via Gateway): http://localhost:8080/api/v1/vehicles"
    echo ""
    echo "Press Ctrl+C to stop the service"
    echo ""
    
    # Run the service
    ./mvnw spring-boot:run
else
    echo ""
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi
