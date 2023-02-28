package com.wks.bpm.engine.camunda;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.mongodb.MongoClientException;
import com.mongodb.client.MongoCollection;
import com.wks.caseengine.db.MongoDataConnection;

@Component
public class MongoHealthCheck implements HealthIndicator {

	@Autowired
	private MongoDataConnection connection;

	@Override
	public Health health() {
		try {
			MongoCollection<Document> collection = connection.getDatabase().getCollection("system");
			collection.countDocuments();
		} catch (MongoClientException ex) {
			return Health.down().withDetail("mongodb", ex.getCause()).build();
		}
		
		return Health.up().build();
	}

}