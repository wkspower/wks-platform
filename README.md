# WKS Platform

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Table of Contents
-----------------
- [Features](#features)
- [Instalation](#instalation)
- [Screenshots](#screenshots)
- [License](#license)
- [Contact](#contact)

WKS Platform is an open-source Case Management and Process Automation solution that leverages a powerful stack of technologies, including Camunda, MongoDB, Keycloak, Traefik, MinIO, OPA (Open Policy Agent), Form.io, Spring Boot, and React. It provides a comprehensive framework for managing and automating business processes, enabling organizations to streamline their operations, enhance efficiency, and improve decision-making.

WKS Platform is ideal for organizations in various industries, including but not limited to:
- Financial Services: Streamline and automate complex financial processes, such as loan approvals, claims management, and risk assessment.
- Healthcare: Efficiently manage patient cases, automate healthcare processes, and ensure compliance with regulatory requirements.
- Insurance: Automate insurance claims processing, underwriting, policy management, and enhance customer service.
- Manufacturing: Streamline production workflows, manage quality control processes, and improve supply chain management.
- Government: Automate administrative processes, citizen service requests, and regulatory compliance procedures.
- Education: Simplify student enrollment, course registration, and academic workflows.

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

## Instalation

To run the project locally in a development environment, we recommend using Docker Compose. This ensures that all the necessary dependencies and services are set up correctly and provides a consistent and reproducible environment.

```bash
git clone https://github.com/wkspower/wks-platform-docker.git
cd wks-platform-docker
git clone https://github.com/wkspower/wks-platform-docker.git
cd wks-platform-docker
docker-compose up -d
```

After successfully running `docker-compose up` to start the Docker Compose configuration, it may take a while for all the web applications to finish their startup process. Once the startup is complete, you can access the following web applications:

- **WKS Platform Portal**: You can access the WKS Platform Portal by opening your web browser and navigating to [http://localhost:3001](http://localhost:3001). Use the following credentials to log in: 
    - Username: demo
    - Password: demo

- **Camunda**: To access the Camunda web application, open your web browser and visit [http://localhost:8080](http://localhost:8080). Use the following credentials to log in: 
    - Username: demo
    - Password: demo

- **Keycloak**: Access the Keycloak web application by opening your web browser and visiting [http://localhost:8082](http://localhost:8082). Use the following credentials to log in as an administrator: 
    - Username: admin
    - Password: admin

- **MinIO**: Access the MinIO web application by opening your web browser and visiting [http://localhost:9000](http://localhost:9000). Use the following credentials to log in as an administrator: 
    - Username: minio
    - Password: #minio00#

Please note that it may take some time for the web applications to become fully operational, especially if this is the first time you are running them. If you encounter any issues, wait a few moments and try accessing the applications again.

Additionally, the MongoDB database used by these applications is running on port 27017.

Remember to ensure that you have Docker installed on your system before running the `docker-compose up` command.

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
