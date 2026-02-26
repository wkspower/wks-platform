#!/bin/bash

# Change directory to two levels up from the script location
cd "$(dirname "$0")"/../..

# Run docker-compose with multiple files
docker-compose -f docker-compose.portal.yaml down --remove-orphans -v "$@"

# Change back to the original directory
cd -
