package com.wks.caseengine.loader.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import lombok.Getter;

@Configuration
@Getter
public class MongoLocalConfigFactory {

	@Value("${spring.data.mongodb.uri}")
	private String dbUrl;

	@Value("${spring.data.mongodb.database}")
	private String dbShared;

	@Value("${spring.data.mongodb.database.tenant}")
	private String dbTenant;

	private MongoClient mongoClient() {
		try {
			ConnectionString conn = new ConnectionString(dbUrl);
			MongoClientSettings mongoClientSettings = MongoClientSettings.builder().applyConnectionString(conn).build();
			return MongoClients.create(mongoClientSettings);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public MongoTemplate mongoTemplateTenant() {
		SimpleMongoClientDatabaseFactory mongoDbFactory = new SimpleMongoClientDatabaseFactory(mongoClient(), dbTenant);
		return new MongoTemplate(mongoDbFactory);
	}

	public MongoTemplate mongoTemplateShared() {
		SimpleMongoClientDatabaseFactory mongoDbFactory = new SimpleMongoClientDatabaseFactory(mongoClient(), dbShared);
		return new MongoTemplate(mongoDbFactory);
	}

}
