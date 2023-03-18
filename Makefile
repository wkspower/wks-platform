servers:
	@docker-compose up --build --remove-orphans mongodb camunda keycloak opa

backend:
	@docker-compose up --build --remove-orphans case-engine-rest-api bpm-engine-c7-external-service email-to-case

frontend:
	make -j 1 case-portal

case-portal:
	@cd $(PWD)/case-portal-react && make

user-guide:
	@cd $(PWD)/website && make

sendmail:
	@curl -i -XPOST http://localhost:8083/email/receive?apiKey=wRSlZt0oNGq6NOzsSu2PdczFsmZXK0Sao4cqCu8mmvdARWTAd3-8QXHROVOnYjOm -H "content-type: multipart/form-data" -F to=contractor-onboarding@new-case.localhost.sendgrid.wkspower.com -F from=1@new-case -F subject=1@new-case -F text= -F html=