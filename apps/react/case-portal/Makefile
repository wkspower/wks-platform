default: install start

install:
	@yarn install

start:
	@GENERATE_SOURCEMAP=false REACT_APP_WEBSOCKETS_CASE_CREATED=case-create REACT_APP_WEBSOCKETS_HUMAN_TASK_CREATED=human-task-create REACT_APP_NOVU_APP_ID=SOME_NOVU_APP_ID PORT=3001 REACT_APP_KEYCLOAK_URL=http://localhost:8082 REACT_APP_API_URL=http://localhost:8081 REACT_APP_EMAIL_URL=http://localhost:8083 REACT_APP_STORAGE_URL=http://localhost:8085 yarn start