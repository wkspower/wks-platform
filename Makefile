servers:
	@docker-compose up --build mongodb camunda camunda-modeler-push keycloak

backend:
	@docker-compose up --build case-engine-rest-api bpm-engine-c7-external-service

frontend:
	make -j 2 case-portal-admim case-portal

case-portal-admim:
	@cd $(PWD)/case-portal-admin-react && make

case-portal:
	@cd $(PWD)/case-portal-react && make

user-guide:
	@cd $(PWD)/website && make	