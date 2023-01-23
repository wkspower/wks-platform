---
id: installation
title: Installation [deprecated]
---

import {AppName, JdkVersion, MavenVersion, NodeVersion} from '@site/src/components/Config';

## Installation

The following instructions will guide you on how to install the project in a local development environment.

### Requirements
These are the following softwares you will have to be installed on your local development environment before running this project:

* Git
* JDK <JdkVersion />
* Maven <MavenVersion />
* Node <NodeVersion />
* Camunda 7

**NOTE 1**: You can also optionaly have [Postman](https://www.postman.com/downloads/) to trigger requests to the project API and [Camunda Modeler](https://camunda.com/download/modeler/) to create BPM processes.

**NOTE 2**: There are a few ways to have a Camunda Instance running in your local environment:
- [Docker](https://hub.docker.com/r/camunda/camunda-bpm-platform/)
- [Camunda Springboot](https://victor-franca.com/2022/02/02/bootstrapping-camunda-with-spring-boot-minimal-configuration/)


### Step 1: Clone the repo from [Github](https://github.com/wkspower/wks-platform)

### Step 2: Backend Installation

#### Step 2.1: In the project root folder, run the following Maven command to install the project dependencies for all Java modules
`mvn package install`

#### Step 2.2: In the <font color="green">case-engine-rest-api</font> project folder, create an environment variables file named <font color="green">env.properties</font> containing the following code:
````
    CAMUNDA7_HOST=<CAMUNDA_INSTANCE_ADDRESS> 
    #ex: CAMUNDA7_HOST=LOCALHOST (not http://LOCALHOST)

    CAMUNDA7_PORT=<CAMUNDA_INSTANCE_PORT>
    #ex: 8080
````

#### Step 2.3: Run the following Maven command to startup the project API
`mvn spring-boot:run -pl case-engine-rest-api`

#### Step 2.4: Test if the Rest API is up by running the following command (Camunda instance needs to be running):
`curl --location --request GET "http://localhost:8081/healthCheck"`

**NOTE 1**: You may change the default Rest API port by replacing the value of server.port property at case-engine-rest-api/src/main/resources/application.yml

**NOTE 2**: You will find a postman colletion for the Rest API exported into the postman-collections folder bellow the project root folder.

### Step 3: Frontend Installation

#### Step 3.1: Simply run `npm install` and then `npm run start` in case-portal-react folder and check if the app is runnning at port 3000.