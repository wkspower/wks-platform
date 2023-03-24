servers:
	@docker compose --profile servers up --build -d

backend:
	@docker compose --profile api up --build -d

frontend:
	@docker compose --profile frontend up --build -d

setup:
	@docker compose --profile init up --build

case-portal:
	@cd $(PWD)/case-portal-react && make

user-guide:
	@cd $(PWD)/website && make

sendmail:
	@curl -i -XPOST http://localhost:8083/email/receive?apiKey=wRSlZt0oNGq6NOzsSu2PdczFsmZXK0Sao4cqCu8mmvdARWTAd3-8QXHROVOnYjOm -H "content-type: multipart/form-data" -F to=contractor-onboarding@new-case.localhost.sendgrid.wkspower.com -F from=1@new-case -F subject=1@new-case -F text= -F html=

certs:
	@cd configs/certs && mkcert "*.wkspower.local" "*.svc.cluster.local" camunda keycloak opa localhost
	@cd configs/certs && openssl pkcs12 -name wks -export -out keystore.p12 -in _wildcard.wkspower.local+5.pem -inkey _wildcard.wkspower.local+5-key.pem -passin pass:wks -passout pass:wks
