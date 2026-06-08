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
package com.wks.caseengine.db;

import java.util.Arrays;
import java.util.Optional;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.wks.api.security.context.SecurityContextTenantHolder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ConditionalOnProperty(name = "database.type", havingValue = "mongo", matchIfMissing = false)
public class EngineMongoDatabaseFactory extends SimpleMongoClientDatabaseFactory {

	@Autowired
	private SecurityContextTenantHolder holder;

	// When multi-tenant (default), a missing tenant fails closed. When single-tenant,
	// fall back to a configurable default database (mirrors the JPA path's "public").
	@Value("${wks.tenancy.multi-tenant:true}")
	private boolean multiTenant;

	@Value("${wks.tenancy.default-tenant:localhost}")
	private String defaultTenant;

	public EngineMongoDatabaseFactory(MongoClient mongoClient, String globalDB) {
		super(mongoClient, globalDB);
	}

	@Override
	public MongoDatabase getMongoDatabase() {
		PojoCodecProvider build = PojoCodecProvider.builder()
				.conventions(Arrays.asList(Conventions.ANNOTATION_CONVENTION)).automatic(true).build();

		CodecRegistry provider = CodecRegistries.fromProviders(build);

		CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
				provider);

		return getMongoClient().getDatabase(getTenantDatabase()).withCodecRegistry(pojoCodecRegistry);
	}

	private String getTenantDatabase() {
		Optional<String> tenantId = holder.getTenantId();

		if (!tenantId.isEmpty()) {
			log.debug("using tenant database {}", tenantId.get());
			return tenantId.get();
		}

		if (!multiTenant) {
			log.debug("no tenant in context; using single-tenant default database {}", defaultTenant);
			return defaultTenant;
		}

		throw new IllegalArgumentException("Could't locate tenant database in session context holder");
	}

}
