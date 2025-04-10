package com.wks.caseengine.utility;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.stereotype.Component;

@Component
public class KeycloakAdminClient {

    public Keycloak getInstance() {
        return KeycloakBuilder.builder()
                .serverUrl("http://keycloak:8080")  // Your Keycloak URL
                .realm("master")                     // Admin realm
                .username("admin")                   // Admin username
                .password("admin")                   // Admin password
                .clientId("admin-cli")               // Client with access
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }
}

