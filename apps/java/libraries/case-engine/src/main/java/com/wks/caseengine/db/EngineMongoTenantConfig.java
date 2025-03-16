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
import java.util.Collections;

import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.SpringDataMongoDB;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

@Configuration
@ConditionalOnProperty(name = "database.type", havingValue = "mongo", matchIfMissing = false)
public class EngineMongoTenantConfig {

	@Autowired
	private EngineMongoSettings props;

	@Bean
	public MongoClient mongoClient() {
		MongoClientSettings settings = mongoClientSettings();
		return MongoClients.create(settings, SpringDataMongoDB.driverInformation());
	}

	@Bean
	public MongoMappingContext mongoMappingContext() {
		return new MongoMappingContext();
	}
	
	@Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Collections.emptyList());
    }	

	@Bean
	public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory factory, MongoMappingContext context,
			ObjectProvider<MongoCustomConversions> conversions) {
		DefaultDbRefResolver dbRefResolver = new DefaultDbRefResolver(factory);
		MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, context);
		converter.setCustomConversions(conversions.getIfAvailable());
		converter.afterPropertiesSet();
		return converter;
	}

	@Bean
	public MongoDatabaseFactory mongoDbFactory() {
		return new EngineMongoDatabaseFactory(mongoClient(), props.getDataBaseName());
	}

	@Bean
	public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MappingMongoConverter converter) {
		return new MongoTemplate(mongoDbFactory, converter);
	}

	@Bean
	public MongoTemplate mongoTemplateShared(EngineMongoSettings fromProps) {
		String db = fromProps.getDataBaseName();
		MongoDatabaseFactory mongoDbFactory = new SimpleMongoClientDatabaseFactory(mongoClient(), db);
		return new MongoTemplate(mongoDbFactory);
	}

	protected String getDatabaseName() {
		return props.getDataBaseName();
	}

	protected MongoClientSettings mongoClientSettings() {
		PojoCodecProvider build = PojoCodecProvider.builder()
				.conventions(Arrays.asList(Conventions.ANNOTATION_CONVENTION)).automatic(true).build();

		CodecRegistry provider = CodecRegistries.fromProviders(build);

		CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
				provider);

		MongoClientSettings.Builder builder = MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(props.getUri())).codecRegistry(pojoCodecRegistry);

		builder.applyToConnectionPoolSettings(pool -> {
			pool.minSize(props.getMinPool());
			pool.maxSize(props.getMaxPool());
		});

		builder.uuidRepresentation(UuidRepresentation.JAVA_LEGACY);
		return builder.build();
	}

}
