# WKS Platform

## Get started

To start a Docker container of WKS Platform:

```
mvn install package
docker-compose up
```

> **_NOTE:_**  If you don't want sample data to be loaded on startup, disable environment variable data.import.enabled inside case-engine-rest-api service at docker-compose.yml file. Sample data files are located at /case-engine-sample-data

## Web Applications and Tools default URLs

#### WKS Web Applications (default user/password: demo/demo)
- WKS Platform Admin: http://localhost:3002 
- WKS Platform Portal: http://localhost:3001

#### Camunda Web Applications (default user/password: demo/demo)
- Camunda Tasklist - Web application for managing and completing user tasks in the context of processes: http://localhost:8080/camunda/app/tasklist/ 
- Camunda Cockpit - Web application tool for process operations: http://localhost:8080/camunda/app/cockpit
- Camunda Admin - Web application for managing users, groups, and their access permissions: http://localhost:8080/camunda/app/admin

#### Process Design
- Camunda Modeler - A [standalone desktop application](https://camunda.com/download/modeler/) that allows business users and developers to design & configure processes.

## Rest APIs

#### Case Management Rest API 
- Url: http://localhost:8081/
- Swagger: http://localhost:8081/swagger-ui/index.html
- Postman Collection: /postman-collections/wks-platform.postman_collection.json

#### Camunda Rest API
- Url: http://localhost:8080/engine-rest/
- Postman Collection: /postman-collections/camunda7.postman_collection.json

#### Demo Users
- john/john
- mary/mary

## What is WKS Platorm?

## Main concepts
1. Case Management
2. Case Definition
3. Case Instance
4. Case Stage
5. BPM Process Engine
6. Process Definition
7. Process Instance
8. Tasks
9. Variables
10. Messages

### Case Management

A method or practice of coordinating work by organizing all of the relevant information into one place - called a case. The case becomes the focal point for assessing the situation, initiating activities and processes, as well as keeping a historical record of what has transpired.

<img src="https://user-images.githubusercontent.com/85225281/209807324-f8a8ab9c-403c-4484-96b5-109bfb999d04.png" width="400">

### Case Definition
A Case Definition is the 'template' for the creation of new Cases Instances. If defines which attributes, stages and processes definitions will be used by Cases Instances created from it

### Case Instance
 A Case Instance is created based in a Case Definition that can be designed and is the 'Digital Folder' for related information, documents, communication and processes for cases. The following image demonstrate the concept of a case:
 
### Case Stage
Cases lifecycles spans over its stages. The case stages are defined in a case definition. The following are stages examples in what could be a "Contract Creation Case":

1. Data Collection
2. Data Analysis
3. Data Approval
4. Contract writing
5. Contract signing

### BPM Process Engine
The BPM process engine is an external component that maganes processes definitions and processes instances. Camunda 7 is the current BPM Engine we integrates with.

### Process Definition
A process definition defines the workflow for a process with its tasks, transictions, gateways, subprocesses and events. This is an example of a process definition using the BPM Notation(BPMN):

![image](https://user-images.githubusercontent.com/85225281/205023159-b1987e14-bc33-43df-8a6e-2d8dc4d1bf05.png)


### Process Instance
A process instance is created from a process definition. There can be many process instances created for 1 specific process definition. Below we have an example of process instances created based on the same process definition in a web application from Camunda called Camunda Cockpit:

![image](https://user-images.githubusercontent.com/85225281/205023226-ebfbc868-ecba-4679-bade-a1a683f8b3c6.png)

### Tasks
A process is composed by many tasks. A Task identifies a unit of work to be done by a human or system actor.

### Variables
During a the execution, a process creates and updates variables values that are used in rules(business rules, conditional logics, etc) and task forms.

### Messages
Messages can be sent to and from processes in a topic-subscription fashion in order to notify or recieve notifications from external systems.


> **_NOTE:_**  See more about BPMN concepts in http://www.omg.org/spec/BPMN/2.0/ and https://docs.camunda.org/manual/7.18/reference/bpmn20/.

## Typical WKS Platform environment

![image](https://user-images.githubusercontent.com/85225281/205024334-fe983dda-6bad-4e1c-8c40-99b739a7fd7a.png)


### Cluster
WKS Platform engine is a stateless micro-service containarized application that runs quikly and reliably from one computing environment to another making it possible to have multiple Engines as per scalability requirements.

### Camunda Cluster (source: https://camunda.com/platform-7/performance)

In order to provide load balancing or fail-over capabilities, the process engine can be distributed to different nodes in a cluster. Each process engine instance will then connect to a shared database.

The individual process engine instances do not maintain session state across transactions. Whenever the process engine runs a transaction, the complete state is flushed out to the shared database. This makes it possible to route subsequent requests which do work in the same process instance to different cluster nodes. This model is very simple and easy to understand and imposes limited restrictions when it comes to deploying a cluster installation. As far as the process engine is concerned there is also no difference between setups for load balancing and setups for fail-over (as the process engine keeps no session state between transactions).

As a consequence, it is extremely easy to set up HA configurations such as active/active nodes.

## Internal Components

### WKS Platform Engine
This stateless service handles all operations to the WKS Platform database and integrates with Camunda and other external systems via REST APIS or topic-subscription

### WKS Platform Rest API
OAS3 Open API for WKS Platform Engine

### Camunda External Service Client
Camunda External Service client to consume processes events(messages)

> **_NOTE:_**  See more about Camunda External Tasks at https://docs.camunda.org/manual/7.18/user-guide/process-engine/external-tasks/.


### E-mail to Case
Read mail boxes and send out e-mails. This component can be configured with Email Templates and rules for Case events definitions based on e-mails. And can also send Emails out based on Camunda Processes events.

### WKS Modeler Templates
WKS Platform provides customized modeler templates to accelerate the process design work. These templates can be found at /bpm-c7-element-template. Follow the following link for how to install and use templates in Camunda Modeler: https://docs.camunda.io/docs/components/modeler/desktop-modeler/element-templates/configuring-templates/

On macOS you have to add them to the folder:
/Users/{user_name}/Library/Application Support/camunda-modeler/resources/element-templates

## Running platform in mode high availability (HA)

### 1 - First step

```bash
# edit
sudo vim /etc/hosts

# copy/past this is entries
127.0.0.1	api.wkspower.local
127.0.0.1	app.wkspower.local
127.0.0.1	login.wspower.local
127.0.0.1	iam.wkspower.local
127.0.0.1   camunda.wkspower.local
::1         api.wkspower.local
::1         app.wkspower.local
::1         login.wkspower.local
::1         iam.wkspower.local
::1         camunda.wkspower.local
```

### 2 - Second step

```bash
make servers
make setup
make api
make frontend
```

## 3 - Final step, scaling services to high availability (HA)

```bash
curl -L https://github.com/FiloSottile/mkcert/releases/download/v1.4.4/mkcert-v1.4.4-darwin-amd64 --output mkcert
chmod +x mkcert
./mkcert --install
docker compose --profile servers up -d --scale keycloak=2
docker compose --profile servers up -d --scale camunda=2
docker compose ps
```