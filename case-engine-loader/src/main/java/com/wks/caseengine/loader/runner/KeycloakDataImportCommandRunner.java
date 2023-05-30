package com.wks.caseengine.loader.runner;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.keycloak.admin.client.Keycloak;
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
	
	@Value("${keycloak.data.import.clientid}")
	private String clientId;
	
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
	
	@Autowired
	private GsonBuilder gsonBuilder;
	
	@Override
	public void run(String... args) throws Exception {
		log.info("Start of data importing");

		Keycloak keycloak = Keycloak.getInstance(
			url, 
			"master", 
			admin, 
			adminPassword, 
			"admin-cli"
		);

		List<ClientRepresentation> clients = new ArrayList<ClientRepresentation>();
		ClientRepresentation client = new ClientRepresentation();
		client.setClientId(clientId);
		client.setPublicClient(true);
		client.setProtocol("openid-connect");
		client.setDirectAccessGrantsEnabled(true);
		client.setStandardFlowEnabled(true);
		client.setFullScopeAllowed(true);
		client.setClientAuthenticatorType("client-secret");
		client.setRedirectUris(Arrays.asList(redirectUrl));
		client.setWebOrigins(Arrays.asList(webOrigins));
		client.setDefaultClientScopes(createDefaultClientScopes());
		client.setOptionalClientScopes(createOptionalClientScopes());
		clients.add(client);
		
		RealmRepresentation realm = new RealmRepresentation();
		realm.setRealm(realmName);
		realm.setUsers(createUsers());
		realm.setClients(clients);
		realm.setClientScopes(createScopes());
		realm.setEnabled(true);
		
		RolesRepresentation roleRepresentation = new RolesRepresentation();
		roleRepresentation.setRealm(createRealmRoles());
		realm.setRoles(roleRepresentation);
		realm.setGroups(createGroups());

		try {
			keycloak.realms().create(realm);
		} catch (Exception e) {
			log.error("error to create keycloack",e);
		}

		log.info("End of data importing");
	}

	private List<String> createOptionalClientScopes() {
		return Arrays.asList(
			"address",
	        "phone",
	        "offline_access",
	        "microprofile-jwt"
		);
	}

	private List<String> createDefaultClientScopes() {
		return Arrays.asList(
			"web-origins",
	        "acr",
	        "org",
	        "roles",
	        "profile",
	        "email"
		);
	}

	private List<UserRepresentation> createUsers() {
		List<UserRepresentation> users = new ArrayList<UserRepresentation>();
		
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
		return gsonBuilder.create().fromJson(new InputStreamReader(stream), new TypeToken<List<GroupRepresentation>>() {}.getType());
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
		List<RoleRepresentation> roles = new ArrayList<RoleRepresentation>();
		out.forEach(r -> {
			roles.add(new RoleRepresentation(r.get("name"), r.get("description"), false));
		});
		return roles;
	}

}
