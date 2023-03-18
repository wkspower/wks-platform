package com.wks.caseengine.db;

import java.util.concurrent.TimeUnit;

import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;

@Configuration
public class EngineMongoTenateConfig extends AbstractMongoClientConfiguration {

	@Autowired
	private EngineMongoSettings props;

	@Override
	protected String getDatabaseName() {
		return props.getDataBaseName();
	}
	
	@Override
	protected MongoClientSettings mongoClientSettings() {
		MongoClientSettings.Builder builder = MongoClientSettings.builder();
		builder.applyToConnectionPoolSettings(pool -> {
			pool.minSize(props.getMinPool());
			pool.maxSize(props.getMaxPool());
			pool.maxConnectionIdleTime(props.getMaxConnectionIdleTime(), TimeUnit.MILLISECONDS);
			pool.maxConnectionLifeTime(props.getMaxConnectionLifeTime(), TimeUnit.MILLISECONDS);
		});
		builder.applyToClusterSettings(c -> {
			c.applyConnectionString(new ConnectionString(props.getUri()));
		});
		builder.uuidRepresentation(UuidRepresentation.JAVA_LEGACY);
		return builder.build();
	}

	@Override
	@Primary
	@Bean
	public MongoDatabaseFactory mongoDbFactory() {
		return new EngineMongoDatabaseFactory(mongoClient(), props.getDataBaseName());
	}

	@Override
	@Bean
	@Primary
	public MongoTemplate mongoTemplate(MongoDatabaseFactory mongoDbFactory, MappingMongoConverter converter) {
		return new MongoTemplate(mongoDbFactory, converter);
	}

	@Bean
	public MongoTemplate mongoTemplateShared(EngineMongoSettings fromProps) {
		String db = fromProps.getDataBaseName();
		MongoDatabaseFactory mongoDbFactory = new SimpleMongoClientDatabaseFactory(mongoClient(), db);
		return new MongoTemplate(mongoDbFactory);
	}
	
}
