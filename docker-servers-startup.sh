#!/bin/bash

# Run docker-compose with multiple files
docker-compose -f docker-compose.yaml -f docker-compose.camunda7.yaml -f docker-compose.demo-data-loader.camunda7.yaml -f docker-compose.event-hub.yaml -f docker-compose.websocket.yaml -f docker-compose.novu.yaml up -d --build "$@"