#!/bin/bash
echo "========================================"
echo "Building Jewellery Microservices"
echo "========================================"

echo ""
echo "Step 1: Building all Maven modules..."
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "Build failed! Exiting..."
    exit 1
fi

echo ""
echo "Step 2: Building Docker images..."
docker-compose build

if [ $? -ne 0 ]; then
    echo "Docker build failed! Exiting..."
    exit 1
fi

echo ""
echo "========================================"
echo "Build Complete!"
echo "========================================"
echo ""
echo "To start all services, run:"
echo "  docker-compose up -d"
echo ""
echo "To view logs:"
echo "  docker-compose logs -f"
echo ""
echo "To stop all services:"
echo "  docker-compose down"
echo ""
