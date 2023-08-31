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
package com.wks.emailtocase.repository.impl;

import java.util.Optional;

import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.client.MongoCollection;
import com.wks.caseengine.db.EngineMongoDataConnection;
import com.wks.emailtocase.caseemail.Organization;
import com.wks.emailtocase.repository.OrganizationRepository;

@Component
public class OrganizationRepositoryImpl implements OrganizationRepository {

	@Autowired
	private EngineMongoDataConnection connection;

	@Autowired
	private GsonBuilder gsonBuilder;

	private MongoCollection<JsonObject> getCollection() {
		return connection.getOrganizationCollection();
	}

	@Override
	public Organization get() {
		Optional<JsonObject> data = Optional.of(getCollection().find().first());
		if (data.isEmpty()) {
			return null;
		}

		Gson gson = gsonBuilder.create();
		return gson.fromJson(data.get().getJson(), Organization.class);
	}

}
