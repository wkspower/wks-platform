package com.wks.caseengine.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wks.caseengine.entity.UserScreenMapping;
import com.wks.caseengine.repository.UserScreenMappingRepository;
import com.wks.caseengine.utility.KeycloakAdminClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KeycloakUserService {

	@Value("${keycloak.realm.name}")
	private String keycloakRealmName;

	@Autowired
	private UserScreenMappingRepository userScreenMappingRepository;
	
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

	public Map<String, Object> updateUser(Map<String, Object> data) throws Exception {
		Map<String, Object> result = new HashMap<>();
		Keycloak keycloak = keycloakAdminClient.getInstance();

//		try {
//			// Fetch user
//			UserResource userResource = keycloak.realm(keycloakRealmName).users().get(userId);
//			UserRepresentation user = userResource.toRepresentation();
//
//			// Update attributes if present
//			Object attrObj = data.get("attributes");
//			if (attrObj instanceof Map) {
//				Map<String, Object> newAttributes = (Map<String, Object>) attrObj;
//				Map<String, List<String>> attributes = Optional.ofNullable(user.getAttributes())
//				        .orElseGet(HashMap::new);
//
//				newAttributes.forEach((key, value) -> {
//				    if (value instanceof String) {
//				        attributes.put(key, Collections.singletonList((String) value));
//				    } else if (value instanceof List) {
//				        List<?> rawList = (List<?>) value;
//				        // Convert list items to string (safely)
//				        List<String> stringList = rawList.stream()
//				                                         .map(String::valueOf)
//				                                         .collect(Collectors.toList());
//				        attributes.put(key, stringList);
//				    } else {
//				        attributes.put(key, Collections.singletonList(String.valueOf(value)));
//				    }
//				});
//
//				user.setAttributes(attributes);
//				userResource.update(user);
//			}
//
//			// Update realm role if provided
//			Object roleObj = data.get("role");
//			if (roleObj instanceof String && !((String) roleObj).isBlank()) {
//				String roleName = (String) roleObj;
//
//				RoleRepresentation roleToAdd = keycloak.realm(keycloakRealmName).roles().get(roleName)
//						.toRepresentation();
//
//				userResource.roles().realmLevel().add(Collections.singletonList(roleToAdd));
//			}
//
//			result.put("status", 200);
//			result.put("message", "User updated successfully.");
//			result.put("data", user);
//
//		} catch (Exception e) {
//			throw new Exception("Failed to update user. Reason: " + e.getMessage(), e);
//		}

		
		try {
		    // Get shared data
		    List<String> userIds = (List<String>) data.get("userIds");
		    Object attrObj = data.get("attributes");
		    Object roleObj = data.get("role");

		    // Step 1: Build plantMapping structure
		    List<Map<String, List<Map<String, List<String>>>>> plantMapping = new ArrayList<>();

		    if (attrObj instanceof Map) {
		        Map<String, Object> attrMap = (Map<String, Object>) attrObj;
		        List<Map<String, Object>> plants = (List<Map<String, Object>>) attrMap.get("plants");

		        for (Map<String, Object> vertical : plants) {
		            String verticalId = (String) vertical.get("verticalId");
		            List<Map<String, Object>> sites = (List<Map<String, Object>>) vertical.get("sites");

		            List<Map<String, List<String>>> siteEntries = new ArrayList<>();

		            for (Map<String, Object> site : sites) {
		                String siteId = (String) site.get("siteId");
		                List<Map<String, Object>> plantList = (List<Map<String, Object>>) site.get("plants");

		                List<String> plantIds = new ArrayList<>();

		                for (Map<String, Object> plant : plantList) {
		                    String plantId = (String) plant.get("plantId");
		                    plantIds.add(plantId);
		                }

		                siteEntries.add(Map.of(siteId, plantIds));
		            }

		            plantMapping.add(Map.of(verticalId, siteEntries));
		        }
		    }

		 // Step 2: Loop through each userId
		    for (String userId : new HashSet<>(userIds)) {
		        UserResource userResource = keycloak.realm(keycloakRealmName).users().get(userId);
		        UserRepresentation user = userResource.toRepresentation();

		        // Prepare mapping for this user
		        List<Map<String, List<Map<String, List<String>>>>> userPlantMapping = new ArrayList<>();

		        if (attrObj instanceof Map) {
		            Map<String, Object> attrMap = (Map<String, Object>) attrObj;
		            List<Map<String, Object>> plants = (List<Map<String, Object>>) attrMap.get("plants");

		            for (Map<String, Object> vertical : plants) {
		                String verticalId = (String) vertical.get("verticalId");
		                List<Map<String, Object>> sites = (List<Map<String, Object>>) vertical.get("sites");

		                List<Map<String, List<String>>> siteEntries = new ArrayList<>();

		                for (Map<String, Object> site : sites) {
		                    String siteId = (String) site.get("siteId");
		                    List<Map<String, Object>> plantList = (List<Map<String, Object>>) site.get("plants");

		                    List<String> plantIds = new ArrayList<>();

		                    for (Map<String, Object> plant : plantList) {
		                        String plantId = (String) plant.get("plantId");
		                        List<String> screens = (List<String>) plant.get("screens");
		                        List<String> permissions = (List<String>) plant.get("permission");

		                        plantIds.add(plantId);

		                        ObjectMapper objectMapper = new ObjectMapper();
		                        String permissionsString = objectMapper.writeValueAsString(permissions);
		                        
		                        for (String screen : screens) {
			                        UserScreenMapping userScreenMapping = new UserScreenMapping();
			                        userScreenMapping.setId(UUID.randomUUID());
			                        userScreenMapping.setUserId(UUID.fromString(userId));
			                        userScreenMapping.setPlantFKId(UUID.fromString(plantId));
			                        userScreenMapping.setVerticalFKId(UUID.fromString(verticalId));
			                        userScreenMapping.setScreenCode(screen);
			                        userScreenMapping.setPermissions(permissionsString);
			                        
			                        userScreenMappingRepository.save(userScreenMapping);
		                        }

		                    }

		                    siteEntries.add(Map.of(siteId, plantIds));
		                }

		                userPlantMapping.add(Map.of(verticalId, siteEntries));
		            }
		        }

		        System.out.println("User screen mapping saved.");

		        // Set user attribute for plantMapping
		        Map<String, List<String>> attributes = Optional.ofNullable(user.getAttributes())
		            .orElseGet(HashMap::new);
		        
		        attributes.put("plants", Collections.singletonList(new ObjectMapper().writeValueAsString(userPlantMapping)));

		        System.out.println("attributes " + attributes.toString());

		        user.setAttributes(attributes);
		        userResource.update(user);

		        System.out.println("User attributes updated..");

		        // Assign realm role
		        if (roleObj instanceof String && !((String) roleObj).isBlank()) {
		            String roleName = (String) roleObj;
		            RoleRepresentation roleToAdd = keycloak.realm(keycloakRealmName).roles().get(roleName)
		                .toRepresentation();
		            userResource.roles().realmLevel().add(Collections.singletonList(roleToAdd));
		        }
		    }

		    result.put("status", 200);
		    result.put("message", "Users updated successfully.");
		    result.put("plantMapping", plantMapping);

		} catch (Exception e) {
		    throw new Exception("Failed to update users. Reason: " + e.getMessage(), e);
		}

		return result;
	}

	public Map<String, Object> getRealmRoles() throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		Keycloak keycloak = keycloakAdminClient.getInstance();

		try {
			List<RoleRepresentation> realmRoles = keycloak.realm(keycloakRealmName).roles().list();

			result.put("status", 200);
			result.put("message", "User roles fetched successfully.");
			result.put("data", realmRoles);
		} catch (Exception ex) {
			throw new Exception("Failed to fetch user roles:" + ex.getMessage(), ex);
		}

		return result;
	}

	public Map<String, Object> getAllGroups() throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		Keycloak keycloak = keycloakAdminClient.getInstance();

		try {
			List<GroupRepresentation> groups = keycloak.realm(keycloakRealmName).groups().groups();;

			result.put("status", 200);
			result.put("message", "User groups fetched successfully.");
			result.put("data", groups);
		} catch (Exception ex) {
			throw new Exception("Failed to fetch user groups:" + ex.getMessage(), ex);
		}

		return result;
	}

	public Map<String, Object> searchUsers(String searchString) throws Exception {
	    Map<String, Object> result = new HashMap<>();
	    Keycloak keycloak = keycloakAdminClient.getInstance(); // assuming this is your singleton

	    try {
	        // This performs a general search across username, first name, last name, and email
	        List<UserRepresentation> users = keycloak.realm(keycloakRealmName)
	                                                 .users()
	                                                 .search(searchString, 0, 100); // optional: start, max

	        result.put("status", 200);
	        result.put("message", "Users fetched successfully.");
	        result.put("data", users);
	    } catch (Exception ex) {
	        throw new Exception("Failed to search users: " + ex.getMessage(), ex);
	    }

	    return result;
	}

}
