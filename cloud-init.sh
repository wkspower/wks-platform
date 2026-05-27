#!/bin/bash
set -e
export DEBIAN_FRONTEND=noninteractive

# 1. Environment Details
PUBLIC_URL="wks-migration-$(date +%s).westus2.cloudapp.azure.com" 
BRANCH="feature/sql-server-migration"
GIT_URL="https://github.com/wkspower/wks-platform.git"

# 2. Install Dependencies
apt-get update && apt-get install -y docker.io git curl openjdk-17-jdk maven
usermod -aG docker ubuntu

# 3. Install Docker Compose
mkdir -p /usr/local/lib/docker/cli-plugins
curl -SL https://github.com/docker/compose/releases/download/v2.24.6/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# 4. Clone and Build
cd /home/ubuntu
git clone -b $BRANCH $GIT_URL wks-platform
cd wks-platform

cd apps/java
./mvnw clean install -DskipTests
cd ../..

# 5. Configure .env
cp .env-sample .env
sed -i "s|REACT_APP_KEYCLOAK_URL=.*|REACT_APP_KEYCLOAK_URL=http://$PUBLIC_URL:8082|" .env
sed -i "s|REACT_APP_API_URL=.*|REACT_APP_API_URL=http://$PUBLIC_URL:8081|" .env
sed -i "s|REACT_APP_STORAGE_URL=.*|REACT_APP_STORAGE_URL=http://$PUBLIC_URL:8085|" .env

# 6. Start Platform
docker compose -f docker-compose.yaml -f docker-compose.camunda7.yaml -f docker-compose.portal.yaml up -d --build
