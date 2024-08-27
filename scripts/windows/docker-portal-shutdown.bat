@echo off
REM Change directory to two levels up from the script location
cd %~dp0\..\..\

REM Run docker-compose with multiple files
docker-compose -f docker-compose.portal.yaml down --remove-orphans -v

REM Change back to the original directory
cd %CD%
