# Migration Summary: MongoDB to Azure SQL Server

This document summarizes the architectural and configuration changes performed to migrate the **WKS Platform** database from MongoDB to Microsoft SQL Server (Azure SQL).

## 1. Backend Dependency Updates
- **SQL Server Driver**: Added `mssql-jdbc` dependency to `apps/java/libraries/case-engine/pom.xml` to enable JDBC connectivity.
- **PostgreSQL Cleanup**: Fixed the scope and versioning of the PostgreSQL driver to ensure runtime stability.
- **Lombok Upgrade**: Updated Lombok to `1.18.30` in the parent `pom.xml` to resolve compilation errors with Java 17.

## 2. Infrastructure & Environment Security
- **Secret Externalization**: All database credentials (JDBC URL, Username, Password) have been moved from hardcoded YAML values to environment variables (`WKS_DB_URL`, `WKS_DB_USER`, `WKS_DB_PASSWORD`).
- **Template Updates**: Updated `.env-sample` with the new SQL Server variables.
- **Docker Compose**:
    - Removed the `mongodb` service and its associated volume from `docker-compose.yaml`.
    - Updated `docker-compose.camunda7.yaml` to remove MongoDB dependencies from `case-engine-rest-api` and `camunda`.
    - Configured the environment mapping to pass local `.env` variables into the containers.

## 3. JPA Configuration & Entity Mapping
- **Package Scan Fix**: Corrected the `EntityScan` package in `EngineDatabaseTenantConfig.java` from `com.wks.caseengine.entity` to `com.wks.caseengine.jpa.entity` to enable Hibernate to find the JPA entities.
- **JPA Property Propagation**: Modified the manual `EntityManagerFactory` creation logic to correctly inject and apply properties defined in `application.yml` (e.g., `ddl-auto`, `dialect`).
- **SQL Server Dialect**: Configured the application to use `org.hibernate.dialect.SQLServerDialect` and explicitly target the `dbo` schema.
- **Type Safety**: Patched `CaseInstanceJpaRepositoryImpl.java` to handle the `CaseStatus` Enum-to-String conversion explicitly, preventing Hibernate 6 criteria type-mismatch exceptions.

## 4. Data Seeding (SQL Server)
- **New Seed Script**: Created `apps/java/libraries/case-engine/src/main/resources/data-sqlserver.sql` containing the initial records for Case Definitions, Forms, Record Types, and Queues.
- **UUID Generation**: Utilized SQL Server's `NEWID()` function in seed scripts to ensure unique identifier constraints are met.
- **Initialization Logic**: Configured Spring Boot's `sql.init` to execute the seed script on startup with `continue-on-error: true` to gracefully handle existing records.

## 5. Deployment Instructions
To run the platform with the new database:
1. Ensure your local `.env` file contains the `WKS_DB_*` credentials.
2. Recompile the Java modules: `cd apps/java && ./mvnw clean install -DskipTests`.
3. Start the containers: `docker compose up -d --build`.

---
*Status: Migration Complete. Verified tables creation and seed data injection on Azure SQL instance.*
