#!/bin/bash
for file in ${1}/*.bpmn
do
    echo INFO -- PUSH:$file TO:${2}
    /usr/bin/curl -X POST -F "upload=@${file}" http://${2}/engine-rest/deployment/create
done