#!/bin/bash
set -e

services=("eureka-server" "config-server" "auth-service" "delivery-service" "tracking-service" "admin-service" "api-gateway")

for service in "${services[@]}"; do
    echo "========================================="
    echo "Building $service..."
    echo "========================================="
    cd "$service"
    chmod +x mvnw || true
    ./mvnw clean package -DskipTests
    cd ..
done

echo "========================================="
echo "All microservices built successfully!"
echo "========================================="
