/*
 * WKS Platform - Open-Source Project
 * 
 * This file is part of the WKS Platform, an open-source project developed by WKS Power.
 * 
 * WKS Platform is licensed under the MIT License.
 * 
 * © 2021 WKS Power. All rights reserved.
 * 
 * For licensing information, see the LICENSE file in the root directory of the project.
 */
package com.wks.caseengine.loader.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wks.caseengine.loader.utils.SecretGenerator;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty("keycloak.data.import.enabled")
@Order(2)
@Slf4j
public class KeycloakDataImportCommandRunner implements CommandLineRunner {

	@Value("${keycloak.data.import.url}")
	private String url;

	@Value("${keycloak.data.import.admin}")
	private String admin;

	@Value("${keycloak.data.import.adminpass}")
	private String adminPassword;

	@Value("${keycloak.data.import.realm}")
	private String realmName;

	@Value("${keycloak.data.import.portal-clientid}")
	private String portalClientId;

	@Value("${keycloak.data.import.externaltasks-clientid}")
	private String externalTasksClientId;

	@Value("${keycloak.data.import.externaltasks-secret}")
	private String externalTasksSecret;

	@Value("${keycloak.data.import.emailtocase-clientid}")
	private String emailToCaseClientId;

	@Value("${keycloak.data.import.emailtocase-secret}")
	private String emailToCaseSecret;

	@Value("${keycloak.data.import.redirecturl}")
	private String redirectUrl;

	@Value("${keycloak.data.import.weborigins}")
	private String webOrigins;

	@Value("${keycloak.data.import.username}")
	private String username;

	@Value("${keycloak.data.import.firstname}")
	private String firstname;

	@Value("${keycloak.data.import.lastname}")
	private String lastname;

	@Value("${keycloak.data.import.email}")
	private String email;

	@Value("${keycloak.data.import.password}")
	private String userPassword;

	@Value("${keycloak.data.import.realm.display.name}")
	private String realmDisplayName;
	
	@Autowired
	private GsonBuilder gsonBuilder;

	@Override
	public void run(String... args) throws IOException {
		log.info("Start of data importing");

		Keycloak keycloak = Keycloak.getInstance(url, "master", admin, adminPassword, "admin-cli");

		List<ClientRepresentation> clients = new ArrayList<>();
		ClientRepresentation portalClient = new ClientRepresentation();
		portalClient.setClientId(portalClientId);
		portalClient.setPublicClient(true);
		portalClient.setProtocol("openid-connect");
		portalClient.setDirectAccessGrantsEnabled(true);
		portalClient.setStandardFlowEnabled(true);
		portalClient.setFullScopeAllowed(true);
		portalClient.setClientAuthenticatorType("client-secret");
		portalClient.setRedirectUris(Arrays.asList(redirectUrl));
		portalClient.setWebOrigins(Arrays.asList(webOrigins));
		portalClient.setDefaultClientScopes(createDefaultClientScopes());
		portalClient.setOptionalClientScopes(createOptionalClientScopes());
		clients.add(portalClient);

		ClientRepresentation externalTasksClient = new ClientRepresentation();
		externalTasksClient.setClientId(externalTasksClientId);
		externalTasksClient.setSecret(externalTasksSecret);
		externalTasksClient.setPublicClient(true);
		externalTasksClient.setProtocol("openid-connect");
		externalTasksClient.setDirectAccessGrantsEnabled(true);
		externalTasksClient.setStandardFlowEnabled(true);
		externalTasksClient.setServiceAccountsEnabled(true);
		externalTasksClient.setAuthorizationServicesEnabled(true);
		externalTasksClient.setFullScopeAllowed(true);
		externalTasksClient.setClientAuthenticatorType("client-secret");
		externalTasksClient.setRedirectUris(Arrays.asList(redirectUrl));
		externalTasksClient.setWebOrigins(Arrays.asList(webOrigins));
		externalTasksClient.setDefaultClientScopes(createDefaultClientScopes());
		externalTasksClient.setOptionalClientScopes(createOptionalClientScopes());
		clients.add(externalTasksClient);

		ClientRepresentation emailToCaseClient = new ClientRepresentation();
		emailToCaseClient.setClientId(emailToCaseClientId);
		emailToCaseClient.setSecret(emailToCaseSecret);
		emailToCaseClient.setPublicClient(true);
		emailToCaseClient.setProtocol("openid-connect");
		emailToCaseClient.setDirectAccessGrantsEnabled(true);
		emailToCaseClient.setStandardFlowEnabled(true);
		emailToCaseClient.setServiceAccountsEnabled(true);
		emailToCaseClient.setAuthorizationServicesEnabled(true);
		emailToCaseClient.setFullScopeAllowed(true);
		emailToCaseClient.setClientAuthenticatorType("client-secret");
		emailToCaseClient.setRedirectUris(Arrays.asList(redirectUrl));
		emailToCaseClient.setWebOrigins(Arrays.asList(webOrigins));
		emailToCaseClient.setDefaultClientScopes(createDefaultClientScopes());
		emailToCaseClient.setOptionalClientScopes(createOptionalClientScopes());
		clients.add(emailToCaseClient);

//		RealmRepresentation realm = new RealmRepresentation();
//		realm.setRealm(realmName);
//		realm.setUsers(createUsers());
//		realm.setClients(clients);
//		realm.setClientScopes(createScopes());
//		realm.setEnabled(true);
//
//		RolesRepresentation roleRepresentation = new RolesRepresentation();
//		roleRepresentation.setRealm(createRealmRoles());
//		realm.setRoles(roleRepresentation);
//		realm.setGroups(createGroups());
//
//		try {
//
//			keycloak.realms().create(realm);
//
//			addUserToGroups(keycloak, externalTasksClientId, Arrays.asList("user", "manager", "email-to-case"));
//			addUserToGroups(keycloak, emailToCaseClientId, Arrays.asList("email-to-case"));
//
//		} catch (Exception e) {
//			log.error("error to create keycloack", e);
//		}

		try {
		    // Check if the realm exists
		    RealmRepresentation existingRealm = null;
		    try {
		        existingRealm = keycloak.realms().realm(realmName).toRepresentation();
		    } catch (Exception e) {
		        // Realm doesn't exist, proceed with creation
		        log.info("Realm does not exist, creating a new realm.");
		    }

		    if (existingRealm == null) {
		        // Create a new realm if it doesn't exist
				RealmRepresentation realm = new RealmRepresentation();

		        realm.setRealm(realmName);
		        realm.setDisplayName(realmDisplayName);
		        realm.setUsers(createUsers());
		        realm.setClients(clients);
		        realm.setClientScopes(createScopes());
		        realm.setEnabled(true);

		        RolesRepresentation roleRepresentation = new RolesRepresentation();
		        roleRepresentation.setRealm(createRealmRoles());
		        realm.setRoles(roleRepresentation);
		        realm.setGroups(createGroups());

		        keycloak.realms().create(realm);
		        log.info("Realm created: " + realmName);
		    } else {
		        // Update the existing realm
		        log.info("Realm exists, updating the realm.");

		        // Update the existing realm properties
		        existingRealm.setDisplayName(realmDisplayName);

		        existingRealm.setUsers(createUsers());
		        existingRealm.setClients(clients);
		        existingRealm.setClientScopes(createScopes());
//		        existingRealm.setRoles(createRealmRoles());
		        existingRealm.setGroups(createGroups());

		        // You may need to update other properties as necessary
		        keycloak.realms().realm(realmName).update(existingRealm);
		        log.info("Realm updated: " + realmName);
		    }

		    // Add users to groups
		    addUserToGroups(keycloak, externalTasksClientId, Arrays.asList("user", "manager", "email-to-case"));
		    addUserToGroups(keycloak, emailToCaseClientId, Arrays.asList("email-to-case"));

		} catch (Exception e) {
		    log.error("Error creating or updating Keycloak realm", e);
		}

		
		log.info("End of data importing");
	}

	private List<String> createOptionalClientScopes() {
		return Arrays.asList("address", "phone", "offline_access", "microprofile-jwt");
	}

	private List<String> createDefaultClientScopes() {
		return Arrays.asList("web-origins", "acr", "org", "roles", "profile", "email");
	}

	private List<UserRepresentation> createUsers() {
		List<UserRepresentation> users = new ArrayList<>();

		UserRepresentation user = new UserRepresentation();
		user.setEnabled(true);
		user.setUsername(username);
		user.setFirstName(firstname);
		user.setLastName(lastname);
		user.setEmail(email);
		user.setGroups(Arrays.asList("user", "manager"));

		CredentialRepresentation password = new CredentialRepresentation();
		password.setTemporary(true);
		password.setType(CredentialRepresentation.PASSWORD);

		if (userPassword == null || userPassword.isBlank()) {
			password.setValue(SecretGenerator.create(16));
		} else {
			password.setValue(userPassword);
		}

		user.setCredentials(Arrays.asList(password));
		users.add(user);

		log.info("Password generated for user name:  {}", password.getValue());

		return users;
	}

	private List<GroupRepresentation> createGroups() {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		InputStream stream = contextClassLoader.getResourceAsStream("realmGroups.json");
		return gsonBuilder.create().fromJson(new InputStreamReader(stream), new TypeToken<List<GroupRepresentation>>() {
		}.getType());
	}

	private List<ClientScopeRepresentation> createScopes() throws IOException {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		InputStream stream = contextClassLoader.getResourceAsStream("clientScopes.json");
		List<ClientScopeRepresentation> clients = gsonBuilder.create().fromJson(new InputStreamReader(stream),
				new TypeToken<List<ClientScopeRepresentation>>() {
				}.getType());
		clients.forEach(f -> {
			if (f.getName().equals("org")) {
				f.getProtocolMappers().get(0).getConfig().put("claim.value", realmName);
			}
		});
		return clients;
	}

	@SuppressWarnings("rawtypes")
	private List<RoleRepresentation> createRealmRoles() throws IOException {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		InputStream stream = contextClassLoader.getResourceAsStream("realmRoles.json");
		List<HashMap<String, String>> out = gsonBuilder.create().fromJson(new InputStreamReader(stream),
				new TypeToken<List<HashMap>>() {
				}.getType());
		List<RoleRepresentation> roles = new ArrayList<>();
		out.forEach(r -> {
			roles.add(new RoleRepresentation(r.get("name"), r.get("description"), false));
		});
		return roles;
	}

	private void addUserToGroups(final Keycloak keycloak, final String userId, List<String>... groupNames) {
		UserRepresentation user = keycloak.realm(realmName).users().search("service-account-" + userId).get(0);
		UserResource userResource = keycloak.realm(realmName).users().get(user.getId());

		// If groupNames is provided, join the user to the specified groups
		if (groupNames != null && groupNames.length > 0) {
			for (List<String> names : groupNames) {
				if (names != null) {
					for (String groupName : names) {
						keycloak.realm(realmName).groups().groups().stream()
								.filter(group -> group.getName().equals(groupName)).findFirst()
								.ifPresent(group -> userResource.joinGroup(group.getId()));
					}
				}
			}
		} else {
			// If no group names provided, add the user to all groups
			keycloak.realm(realmName).groups().groups().forEach(group -> userResource.joinGroup(group.getId()));
		}
	}

}
