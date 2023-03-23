servers:
	@docker compose up --build mongodb postgres camunda keycloak opa opa-bundles-service traefik

backend:
	@docker compose up --build bpm-engine-c7-external-service case-engine-rest-api email-to-case

frontend:
	@make -j 1 case-portal

loader:
	@docker compose --profile=loader up --build case-engine-loader	

case-portal:
	@cd $(PWD)/case-portal-react && make

user-guide:
	@cd $(PWD)/website && make

sendmail:
	@curl -i -XPOST http://localhost:8083/email/receive?apiKey=wRSlZt0oNGq6NOzsSu2PdczFsmZXK0Sao4cqCu8mmvdARWTAd3-8QXHROVOnYjOm -H "content-type: multipart/form-data" -F to=contractor-onboarding@new-case.localhost.sendgrid.wkspower.com -F from=1@new-case -F subject=1@new-case -F text= -F html=

certs:
	@cd configs/certs && mkcert "*.wkspower.local" "*.svc.cluster.local" camunda keycloak opa localhost
	@cd configs/certs && openssl pkcs12 -name wks -export -out keystore.p12 -in _wildcard.wkspower.local+5.pem -inkey _wildcard.wkspower.local+5-key.pem -passin pass:wks -passout pass:wks
