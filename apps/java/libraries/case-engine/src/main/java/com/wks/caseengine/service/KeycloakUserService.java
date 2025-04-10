package com.wks.caseengine.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import com.wks.caseengine.utility.KeycloakAdminClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KeycloakUserService {

	private final KeycloakAdminClient keycloakAdminClient;

	public KeycloakUserService(KeycloakAdminClient keycloakAdminClient) {
		this.keycloakAdminClient = keycloakAdminClient;
	}

	public Map<String, Object> getUsers(String realm) {
		Map<String, Object> result = new HashMap<String, Object>();
		
		Keycloak keycloak = keycloakAdminClient.getInstance();
		List<UserRepresentation> summaryUsers = keycloak.realm(realm).users().list();

		List<UserRepresentation> fullUsers = summaryUsers.stream()
		    .map(user -> keycloak.realm(realm).users().get(user.getId()).toRepresentation())
		    .collect(Collectors.toList());

		result.put("status", 200);
		result.put("message", "Users list by realm " + realm + ".");
		result.put("data", fullUsers);

		return result;
	}

	public Map<String, Object> updateUserAttributes(String realm, String userId, Map<String, String> newAttributes) {
		Map<String, Object> result = new HashMap<String, Object>();

		Keycloak keycloak = keycloakAdminClient.getInstance();

		// Fetch the user
		UserRepresentation user = keycloak.realm(realm).users().get(userId).toRepresentation();

		// Safely initialize attribute map
		Map<String, List<String>> attributes = Optional.ofNullable(user.getAttributes())
		        .orElseGet(HashMap::new);


	    // Update/add attributes
	    newAttributes.forEach((key, value) -> 
	        attributes.put(key, Collections.singletonList(value))
	    );

		user.setAttributes(attributes);

		// Update user
		keycloak.realm(realm).users().get(userId).update(user);
		
		result.put("status", 200);
		result.put("message", "User attributes updated successfully.");
		result.put("data", user);

		return result;
	}
}
