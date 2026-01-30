#!/bin/bash

echo "🔨 Building specific Java services only..."

echo "Building case-engine-rest-api service..."
cd ../../apps/java/services/case-engine-rest-api
mvn clean
if [ $? -ne 0 ]; then
    echo "❌ Failed to clean case-engine-rest-api"
    exit 1
fi
mvn install -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Failed to build case-engine-rest-api"
    exit 1
fi
echo "✅ case-engine-rest-api built successfully"

echo "Building case-engine library..."
cd ../../libraries/case-engine
mvn clean
if [ $? -ne 0 ]; then
    echo "❌ Failed to clean case-engine library"
    exit 1
fi
mvn install -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Failed to build case-engine library"
    exit 1
fi
echo "✅ case-engine library built successfully"

echo "🔄 Restarting Docker servers..."
cd ../../../../scripts/Linux
sh docker-servers-startup.sh

if [ $? -eq 0 ]; then
    echo "🎉 Build and restart completed successfully!"
else
    echo "❌ Failed to restart Docker servers"
    exit 1
fi