#!/bin/bash

# Run docker-compose with multiple files
docker-compose -f docker-compose.portal.yaml up -d --build "$@"