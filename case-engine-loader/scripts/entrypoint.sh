#!/bin/sh

echo "Starting upload model to camunda...."
for file in ${IMPORT_DIR_PATH}/*.bpmn
do
    echo INFO -- PUSH:$file TO:${CAMUNDA_BASE_URL}
    /usr/bin/curl -s -X POST -F "upload=@${file}" ${CAMUNDA_BASE_URL}/deployment/create
done
echo "Finish upload model to camunda"


echo "Start import data samples...."
java -jar /app.jar
echo "Finish import data samples"