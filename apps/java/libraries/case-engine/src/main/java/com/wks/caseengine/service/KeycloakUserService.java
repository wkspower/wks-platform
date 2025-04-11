package com.wks.caseengine.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
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

	@Value("${keycloak.realm.name}")
	private String keycloakRealmName;

	private final KeycloakAdminClient keycloakAdminClient;

	public KeycloakUserService(KeycloakAdminClient keycloakAdminClient) {
		this.keycloakAdminClient = keycloakAdminClient;
	}

	public Map<String, Object> getUsers() throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();

		try {
			Keycloak keycloak = keycloakAdminClient.getInstance();
			List<UserRepresentation> summaryUsers = keycloak.realm(keycloakRealmName).users().list();

			List<Map<String, Object>> userDetails = summaryUsers.stream()
			    .map(user -> {
			        String userId = user.getId();
			        UserResource userResource = keycloak.realm(keycloakRealmName).users().get(userId);
			        UserRepresentation userRep = userResource.toRepresentation();

			        // Directly assigned realm roles
			        List<RoleRepresentation> realmRoles = userResource.roles().realmLevel().listEffective(false); // false -> only direct
			        List<String> realmRoleNames = realmRoles.stream()
			                .map(RoleRepresentation::getName)
			                .collect(Collectors.toList());

			        // Construct response map
			        Map<String, Object> userMap = new HashMap<>();
			        userMap.put("user", userRep);
			        userMap.put("realmRoles", realmRoleNames);
			        return userMap;
			    })
			    .collect(Collectors.toList());


			result.put("status", 200);
			result.put("message", "Users list by realm " + keycloakRealmName + ".");
			result.put("data", userDetails);
		} catch (Exception ex) {
			throw new Exception("Failed to fetch users from keyclok: " + ex.getMessage(), ex);
		}

		return result;
	}

	public Map<String, Object> updateUser(String userId, Map<String, Object> data) throws Exception {
		Map<String, Object> result = new HashMap<>();
		Keycloak keycloak = keycloakAdminClient.getInstance();

		try {
			// Fetch user
			UserResource userResource = keycloak.realm(keycloakRealmName).users().get(userId);
			UserRepresentation user = userResource.toRepresentation();

			// Update attributes if present
			Object attrObj = data.get("attributes");
			if (attrObj instanceof Map) {
				Map<String, Object> newAttributes = (Map<String, Object>) attrObj;
				Map<String, List<String>> attributes = Optional.ofNullable(user.getAttributes())
				        .orElseGet(HashMap::new);

				newAttributes.forEach((key, value) -> {
				    if (value instanceof String) {
				        attributes.put(key, Collections.singletonList((String) value));
				    } else if (value instanceof List) {
				        List<?> rawList = (List<?>) value;
				        // Convert list items to string (safely)
				        List<String> stringList = rawList.stream()
				                                         .map(String::valueOf)
				                                         .collect(Collectors.toList());
				        attributes.put(key, stringList);
				    } else {
				        attributes.put(key, Collections.singletonList(String.valueOf(value)));
				    }
				});

				user.setAttributes(attributes);
				userResource.update(user);
			}

			// Update realm role if provided
			Object roleObj = data.get("role");
			if (roleObj instanceof String && !((String) roleObj).isBlank()) {
				String roleName = (String) roleObj;

				RoleRepresentation roleToAdd = keycloak.realm(keycloakRealmName).roles().get(roleName)
						.toRepresentation();

				userResource.roles().realmLevel().add(Collections.singletonList(roleToAdd));
			}

			result.put("status", 200);
			result.put("message", "User updated successfully.");
			result.put("data", user);

		} catch (Exception e) {
			throw new Exception("Failed to update user. Reason: " + e.getMessage(), e);
		}

		return result;
	}

	public Map<String, Object> getRealmRoles() throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		Keycloak keycloak = keycloakAdminClient.getInstance();

		try {
			List<RoleRepresentation> realmRoles = keycloak.realm(keycloakRealmName).roles().list();

			result.put("status", 200);
			result.put("message", "User attributes updated successfully.");
			result.put("data", realmRoles);
		} catch (Exception ex) {
			throw new Exception("Failed to fetch user roles:" + ex.getMessage(), ex);
		}

		return result;
	}
}
