# WKS Platform - Azure Deployment Guide (SQL Server Migration)

This guide summarizes the successful deployment of the **WKS Platform** (`feature/sql-server-migration` branch) to an **Azure Virtual Machine** using **Azure SQL Database**.

## Phase 1: Azure Infrastructure Creation

### 1. Login and Environment Setup
Run these commands from your local machine (where Azure CLI is installed):

```bash
az login

# Set unique labels and region
export REGION="westus2"
export DNS_LABEL="wks-migration-1775728810" # Use your unique label
export PUBLIC_URL="$DNS_LABEL.$REGION.cloudapp.azure.com"
```

### 2. Create the Setup Script (`cloud-init.sh`)
Create a local file named `cloud-init.sh` to automate the VM preparation:

```bash
cat << 'EOF' > cloud-init.sh
#!/bin/bash
set -e
export DEBIAN_FRONTEND=noninteractive

# 1. Install Dependencies
apt-get update && apt-get install -y docker.io git curl openjdk-17-jdk maven
usermod -aG docker ubuntu

# 2. Install Docker Compose
mkdir -p /usr/local/lib/docker/cli-plugins
curl -SL https://github.com/docker/compose/releases/download/v2.24.6/docker-compose-linux-x86_64 -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# 3. Clone the Repository
cd /home/ubuntu
git clone -b feature/sql-server-migration https://github.com/wkspower/wks-platform.git wks-platform
EOF
```

### 3. Provision the VM
```bash
az group create --name wks-platform-rg --location $REGION

az vm create \
  --resource-group wks-platform-rg \
  --name wks-azure-migration \
  --image Canonical:ubuntu-24_04-lts:server:latest \
  --size Standard_D4s_v3 \
  --admin-username ubuntu \
  --generate-ssh-keys \
  --public-ip-address-dns-name $DNS_LABEL \
  --location $REGION \
  --custom-data @cloud-init.sh
```

### 4. Open Network Ports
```bash
az vm open-port --resource-group wks-platform-rg --name wks-azure-migration --port 3001,8080-8085 --priority 1010
```

---

## Phase 2: Manual Recovery & Application Build
*Note: We performed these manually because the automated script failed at the Maven step.*

1. **SSH into the VM:**
   ```bash
   ssh ubuntu@wks-migration-1775728810.westus2.cloudapp.azure.com
   ```

2. **Fix Ownership and Build Java Apps:**
   ```bash
   sudo chown -R ubuntu:ubuntu /home/ubuntu/wks-platform
   cd /home/ubuntu/wks-platform/apps/java
   mvn clean install -DskipTests
   ```

3. **Configure the Environment (`.env`):**
   ```bash
   cd /home/ubuntu/wks-platform
   cp .env-sample .env
   nano .env
   ```
   **Required Edits:**
   * **WKS Database:** Enter your Azure SQL JDBC URL, User, and Password.
   * **Keycloak URLs:** Update to use your `PUBLIC_URL` for `REACT_APP_` variables.
   * **Internal URLs:** Ensure `KEYCLOAK_URL` points to `http://keycloak:8080`.

4. **Start the Platform:**
   ```bash
   docker compose -f docker-compose.yaml -f docker-compose.camunda7.yaml -f docker-compose.portal.yaml up -d --build
   ```

---

## Phase 3: Keycloak Multi-Tenant Configuration
*This ensures the security tokens match your Azure URL and include necessary permissions.*

### 1. Create Realm and User
```bash
export SUBDOMAIN="wks-migration-1775728810"
export KC_CONTAINER=$(sudo docker ps -qf "name=keycloak")

# Login to Keycloak CLI
sudo docker exec $KC_CONTAINER /opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password admin

# Create Realm and User
sudo docker exec $KC_CONTAINER /opt/keycloak/bin/kcadm.sh create realms -s realm=$SUBDOMAIN -s enabled=true -s sslRequired=NONE || true
sudo docker exec $KC_CONTAINER /opt/keycloak/bin/kcadm.sh create users -r $SUBDOMAIN -s username=demo -s enabled=true
sudo docker exec $KC_CONTAINER /opt/keycloak/bin/kcadm.sh set-password -r $SUBDOMAIN --username demo --new-password demo --temporary false
```

### 2. Configure Roles, Groups, and Claims
```bash
# Create Essential Roles
ROLES=("client_case" "mgmt_bpm_engine" "mgmt_case_def" "client_task" "mgmt_process_engine" "mgmt_record_type" "client_record" "mgmt_bpm_engine_type" "mgmt_form")
for role in "${ROLES[@]}"; do sudo docker exec $KC_CONTAINER /opt/keycloak/bin/kcadm.sh create roles -r $SUBDOMAIN -s name=$role || true; done

# Create Groups and Assign Roles
sudo docker exec $KC_CONTAINER /opt/keycloak/bin/kcadm.sh create groups -r $SUBDOMAIN -s name=user || true
sudo docker exec $KC_CONTAINER /opt/keycloak/bin/kcadm.sh add-roles -r $SUBDOMAIN --gname user --rolename client_case --rolename client_task --rolename client_record

# Add Protocol Mapper (The 'org' claim)
SCOPE_ID=$(sudo docker exec $KC_CONTAINER /opt/keycloak/bin/kcadm.sh get client-scopes -r $SUBDOMAIN --query name=org --fields id --format csv --noquotes | head -n 1 | tr -d '\r\n[:space:]')
sudo docker exec $KC_CONTAINER /opt/keycloak/bin/kcadm.sh create "client-scopes/$SCOPE_ID/protocol-mappers/models" -r $SUBDOMAIN -s name=org -s protocol=openid-connect -s protocolMapper=oidc-hardcoded-claim-mapper -s config="{\"claim.name\":\"org\", \"claim.value\":\"$SUBDOMAIN\", \"jsonType.label\":\"String\", \"access.token.claim\":\"true\", \"id.token.claim\":\"true\"}"

# Fix Issuer URL
sudo docker exec $KC_CONTAINER /opt/keycloak/bin/kcadm.sh update realms/$SUBDOMAIN -s "attributes.frontendUrl=http://wks-migration-1775728810.westus2.cloudapp.azure.com:8082/"
```

---

## Phase 4: Final Verification
1. **Restart API:** `docker compose up -d case-engine-rest-api`
2. **Access Portal:** `http://wks-migration-1775728810.westus2.cloudapp.azure.com:3001`
3. **Login:** Use `demo` / `demo`.

---

## Lessons Learned (What didn't work)
* **`cloud-init` Scripting:** Avoid using `./mvnw` in folders where it isn't present; use system `mvn`.
* **Bash Expansion:** The `!` in `#!/bin/bash` causes errors in `az vm create` if passed as a string; always use the `@filename` syntax.
* **Keycloak Automation:** The `demo-data-loader` is currently tied to MongoDB; manual Keycloak CLI commands are required when using SQL Server.
* **JWT Issuer:** For multi-tenant URLs, the `frontendUrl` attribute in Keycloak is mandatory to prevent 401 Unauthorized errors in the API.
