# WKS Platform

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

> ## ⚠️ CRITICAL NOTE — Spring Boot 4 & Camunda 7 Compatibility
> **Action required:** plan your migration to Spring Boot 4.x to avoid CI/CD interruptions and security-driven deployment blocks.
> Camunda 7 environments require targeted architectural refactoring to remain compliant with modern security scanners.
>
> [![Contact for a Transition Action Plan](https://img.shields.io/badge/Get%20the%20Transition%20Action%20Plan-orange?style=for-the-badge)](https://wkspower.com/contact-us-wks-platform)

[On-line documentation](https://docs.wkspower.com/docs/Introduction/)

[Contact Form](https://share-eu1.hsforms.com/1tpt0kdYDS5CbimQTH7xmVA2dcag3)

[Subscribe for news & updates](https://share-eu1.hsforms.com/1gpWZRXQwSoWQNgCeuztetQ2dcag3)

Table of Contents
-----------------
- [WKS Platform](#wks-platform)
  - [Table of Contents](#table-of-contents)
  - [Features](#features)
  - [Installation](#installation)
  - [Diagrams](#diagrams)
    - [Architecture overview](#architecture-overview)
    - [Case Definition structure](#case-definition-structure)
    - [Event Hub](#event-hub)
  - [Screenshots](#screenshots)
  - [License](#license)
  - [Contact](#contact)

WKS Platform is an open-source Case Management and Process Automation solution that leverages a powerful stack of technologies, including Camunda, MongoDB, Keycloak, Traefik, MinIO, OPA (Open Policy Agent), Form.io, Spring Boot, and React. It provides a comprehensive framework for managing and automating business processes, enabling organizations to streamline their operations, enhance efficiency, and improve decision-making.

WKS Platform is ideal for organizations in various industries, including but not limited to:
- **Financial Services**: Streamline and automate complex financial processes, such as loan approvals, claims management, and risk assessment.
- **Healthcare**: Efficiently manage patient cases, automate healthcare processes, and ensure compliance with regulatory requirements.
- **Insurance**: Automate insurance claims processing, underwriting, policy management, and enhance customer service.
- **Manufacturing**: Streamline production workflows, manage quality control processes, and improve supply chain management.
- **Government**: Automate administrative processes, citizen service requests, and regulatory compliance procedures.
- **Education**: Simplify student enrollment, course registration, and academic workflows.

These are just a few examples of the industry-specific scenarios where WKS Platform can be effectively utilized. Its flexibility and extensibility make it suitable for a wide range of use cases.

WKS Platform is designed as a multi-tenant solution, allowing multiple organizations or departments to use the platform while ensuring data isolation and separation. It provides a secure and customizable environment for each tenant, enabling them to manage their cases, automate processes, and make data-driven decisions within their own dedicated space.

## Features

- **Case Management**: WKS Platform offers a robust case management system that allows users to track and manage cases throughout their lifecycle. It provides features such as case creation, assignment, status tracking, activity logging, and case resolution.

- **Process Automation**: With Camunda at its core, WKS Platform enables the automation of complex business processes. Users can design, model, and execute workflows, define process steps and decision points, and monitor process instances in real-time.

- **Intuitive User Interface**: WKS Platform incorporates a responsive and user-friendly React-based frontend interface. It provides a rich set of features, including task management, case visualization, process monitoring, and reporting, ensuring an intuitive user experience.

- **Dynamic Form Creation**: By leveraging Form.io, WKS Platform allows users to design and create dynamic forms that adapt to specific case requirements. This empowers organizations to collect structured data efficiently, ensuring data consistency and enabling streamlined processes.

- **Data Persistence**: Leveraging MongoDB, WKS Platform ensures reliable and scalable data storage for case-related information. This facilitates efficient retrieval and analysis of data to gain insights and support decision-making.

- **Identity and Access Management**: Integration with Keycloak offers robust identity and access management capabilities, including user authentication, authorization, and role-based access control. This ensures secure access to the platform and its features, protecting sensitive information.

- **Policy Enforcement**: WKS Platform integrates with OPA (Open Policy Agent) to enforce fine-grained policies across the system. Policies can be defined to control access, validate data, enforce business rules, and ensure compliance with organizational regulations.

- **MinIO Integration**: WKS Platform integrates with MinIO, an object storage server, for efficient and scalable storage of files and attachments associated with cases and processes.

- **E-mail to Case**: WKS Platform includes a comprehensive E-mail to Case feature that enables seamless integration between email communication and case management. Users can not only create cases from incoming emails but also receive case-related updates and notifications via email, ensuring efficient and effective communication throughout the case lifecycle. 

- **Robust Backend**: Built on the Spring Boot framework, WKS Platform provides a scalable and high-performance backend infrastructure. It offers reliable API endpoints, data integration capabilities, and supports extensibility through modular design principles.
  
- **Robust API Security with Trust-based Architecture**: WKS Platform prioritizes security and follows the principles of Zero Trust architecture. It ensures safe API communications by implementing secure protocols, encryption, and authentication mechanisms.

- **Traefik Integration**: WKS Platform seamlessly integrates with Traefik, a modern reverse proxy and load balancer, to provide scalable and secure routing of HTTP traffic to the platform's components.

- **Multi-Language Support**: WKS Platform is designed to be a multi-language project, utilizing internationalization (i18n) techniques. This allows for the localization of the platform's interface, making it accessible and usable in different languages. Currently, it supports English and Brazilian Portuguese, with the potential to expand support for additional languages. 

## Installation

> **⚠️ The Docker Compose stack in this repo is for LOCAL DEVELOPMENT only** — it is not
> production-hardened. A separate production path lives under `docker/`.

The local environment is a **single `docker-compose.yml`** driven by Compose profiles. The
minimum is the default, and app images are pulled from the public GitHub Container Registry
(`ghcr.io/wkspower/*`) — **no Maven/Yarn build required**.

```bash
git clone https://github.com/wkspower/wks-platform.git
cd wks-platform
cp .env-sample .env
docker compose up -d            # backend + case portal + demo seed
```

On first run the `demo-data-loader` bootstraps the Keycloak realm and a `demo`
user and seeds sample cases/processes — wait for it to finish, then open
[http://localhost:3001](http://localhost:3001) and log in with `demo` / `demo`.

To start the full stack **without the demo bootstrap** — drop only the `demo`
profile (you keep Keycloak/Mongo/OPA, but get no realm/user until you seed, so no
login yet):

```bash
COMPOSE_PROFILES=mongodb,keycloak,opa,camunda,storage,app,portal docker compose up -d
```

> ⚠️ `COMPOSE_PROFILES=app,portal` on its own does **not** work: the `app` service
> still defaults to Keycloak + OPA + Mongo + Camunda, so without that infra it
> can't start or log in. To run app+portal alone, use the minimal-core toggles
> below.

### Minimal core — zero external containers

Because the cross-cutting concerns are independently toggleable, the case engine
+ REST API + portal can run with **no Mongo, Keycloak, OPA, Camunda or MinIO** —
embedded H2 datastore, an in-process dev-token issuer (the portal auto-logs in,
no Keycloak), authorization off, no workflow engine. Build from source
(`--build`; the published `v1.4.x` images predate these toggles):

```bash
COMPOSE_PROFILES=app,portal \
WKS_SPRING_PROFILES=db-h2 WKS_AUTH_MODE=dev-token WKS_AUTHZ_OPA_ENABLED=false \
WKS_BPM_ENGINE=none WKS_TENANCY_MULTI_TENANT=false WKS_SEED_ENABLED=true \
REACT_APP_AUTH_MODE=dev-token REACT_APP_AUTH_ISSUER_URL=http://localhost:8081/dev-auth \
docker compose up -d --build
```

Then open [http://localhost:3001](http://localhost:3001) — the dev-token issuer
logs you in automatically (no credentials). The same knobs work individually, so
you can enable one concern at a time (e.g. keep Mongo but drop Keycloak); they are
also pre-documented in `.env-sample`.

#### Add MinIO-free attachments (filesystem storage)

To also run attachments without MinIO, just add the `storage-fs` profile and the
filesystem driver — no extra issuer wiring needed. In dev-token mode every backend
defaults `WKS_DEVTOKEN_ISSUER_URL` to the engine's compose service name, so
`storage-api` validates tokens against the engine over the Docker network out of
the box (the browser keeps minting via `localhost`; only the signature is checked,
not the `iss`):

```bash
COMPOSE_PROFILES=app,portal,storage-fs \
WKS_SPRING_PROFILES=db-h2 WKS_AUTH_MODE=dev-token WKS_AUTHZ_OPA_ENABLED=false \
WKS_BPM_ENGINE=none WKS_TENANCY_MULTI_TENANT=false WKS_SEED_ENABLED=true \
REACT_APP_AUTH_MODE=dev-token REACT_APP_AUTH_ISSUER_URL=http://localhost:8081/dev-auth \
DRIVER_STORAGE_FACTORYCLASS=filesystem REACT_APP_STORAGE_MODE=filesystem \
docker compose up -d --build
```

Optional capabilities are profiles you turn on:

| Profile | Adds | Enable with |
| --- | --- | --- |
| `app` *(default)* | case-engine-rest-api backend (port 8081) | on by default |
| `portal` *(default)* | React case portal (port 3001) | on by default |
| `demo` *(default)* | Bootstrap Keycloak login + seed sample data, then exit | on by default; drop it (keep the rest) with the "without the demo bootstrap" command above |
| `notifications` | Kafka + email / websocket / Novu | `KAFKA_ENABLED=true docker compose --profile app --profile portal --profile demo --profile notifications up -d` |
| `proxy` | Traefik reverse proxy | `docker compose --profile app --profile portal --profile demo --profile proxy up -d` |

To compile from source instead of pulling images: `docker compose up -d --build`
(multi-stage Docker builds — still no host Maven needed).

Full guide: [Installation docs](https://docs.wkspower.com/docs/Installation/).
Camunda 8 is experimental/outdated — see `experimental/camunda8/`.

## Diagrams 

### Architecture overview
<img width="840" alt="image" src="https://github.com/wkspower/wks-platform/assets/85225281/323e4811-2a44-4c23-9d38-1f99942dcae5">


### Case Definition structure
<img width="1507" alt="case-definition-structure" src="https://github.com/wkspower/wks-platform/assets/85225281/d478345f-8192-4196-ae53-868151363cf1">

### Event Hub
![image](https://github.com/wkspower/wks-platform/assets/85225281/1f393c1a-84b7-42d3-971b-0e9a49240d27)


## Screenshots

Form Designer
<img width="1487" alt="image" src="https://github.com/wkspower/wks-platform/assets/85225281/9e28708b-0547-4e5f-bee1-5721f63186e7">

Case kanban
<img width="1507" alt="image" src="https://github.com/wkspower/wks-platform/assets/85225281/19ac1611-4328-466b-8574-72a33486edac">

Task List in a Case
<img width="1511" alt="image" src="https://github.com/wkspower/wks-platform/assets/85225281/0c437058-f63e-4d9a-b207-6758aa6a2486">

Task Form
<img width="1501" alt="image" src="https://github.com/wkspower/wks-platform/assets/85225281/55db2e63-64a9-45b8-8d3b-b231f4ac2c31">

Process diagram in Task Form
<img width="1488" alt="image" src="https://github.com/wkspower/wks-platform/assets/85225281/40342c86-451f-40cc-b820-3ae9ee9fc24e">

Create ad-hoc tasks in cases
<img width="1512" alt="image" src="https://github.com/wkspower/wks-platform/assets/85225281/cb98d635-66bd-4877-bcbc-06bac437eb45">

## License

WKS Platform is released under the [MIT License](LICENSE), allowing users to freely use, modify, and distribute the solution as per the terms of the license.

## Contact

For any questions, feedback, or contributions, please reach out to the project team:

- Email: victor@wkspower.com
