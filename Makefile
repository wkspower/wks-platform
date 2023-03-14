servers:
	@docker-compose up --build --remove-orphans mongodb camunda keycloak case-engine-loader opa

backend:
	@docker-compose up --build --remove-orphans case-engine-rest-api bpm-engine-c7-external-service email-to-case

api:
	@docker-compose up --build --remove-orphans case-engine-rest-api

listener:
	@docker-compose up --build --remove-orphans bpm-engine-c7-external-service email-to-case

frontend:
	make -j 1 case-portal

case-portal-admim:
	@cd $(PWD)/case-portal-admin-react && make

case-portal:
	@cd $(PWD)/case-portal-react && make

user-guide:
	@cd $(PWD)/website && make	