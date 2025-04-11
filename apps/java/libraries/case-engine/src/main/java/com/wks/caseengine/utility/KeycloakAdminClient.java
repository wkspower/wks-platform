package com.wks.caseengine.utility;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakAdminClient {

	@Value("${keycloak.url}")
	private String keycloakURL;
	
	@Value("${keycloak.admin}")
	private String keycloakAdmin;
	
	@Value("${keycloak.admin.password}")
	private String keycloakAdminPassword;
	
    public Keycloak getInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakURL)  // Your Keycloak URL
                .realm("master")                     // Admin realm
                .username(keycloakAdmin)                   // Admin username
                .password(keycloakAdminPassword)                   // Admin password
                .clientId("admin-cli")               // Client with access
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }
}

