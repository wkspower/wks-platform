#!/bin/bash
cd ../../apps/java
mvn clean
mvn install -DskipTests
cd ../../scripts/Linux
sh docker-servers-startup.sh