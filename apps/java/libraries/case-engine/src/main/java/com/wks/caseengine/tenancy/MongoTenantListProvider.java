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
package com.wks.caseengine.tenancy;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoClient;
import com.wks.caseengine.db.EngineMongoSettings;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "mongo")
public class MongoTenantListProvider implements TenantListProvider {

	@Autowired
	private MongoClient mongoClient;

	@Autowired
	private EngineMongoSettings props;

	@Override
	public List<String> getTenantIds() {
		List<String> tenants = new ArrayList<>();
		for (String dbName : mongoClient.listDatabaseNames()) {
			if (!dbName.equals("admin") 
					&& !dbName.equals("config") 
					&& !dbName.equals("local") 
					&& !dbName.equals(props.getDataBaseName())) {
				tenants.add(dbName);
			}
		}
		return tenants;
	}
}
