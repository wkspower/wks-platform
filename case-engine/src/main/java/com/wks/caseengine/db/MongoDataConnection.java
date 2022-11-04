package com.wks.caseengine.db;

import java.util.Arrays;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.Conventions;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.json.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import lombok.extern.slf4j.Slf4j;

/**
 * @author victor.franca
 *
 */
@Component
@Slf4j
public class MongoDataConnection {

	private MongoClient mongoClient;
	private MongoDatabase database;

	private MongoCollection<JsonObject> caseInstCollection;
	private MongoCollection<JsonObject> formCollection;
	private MongoCollection<JsonObject> caseDefCollection;
	private MongoCollection<JsonObject> recordTypeCollection;

	private MongoCollection<JsonObject> bpmEngineCollection;

	public MongoDataConnection(@Value("${mongo.connection-string}") String mongoConnectionString,
			@Value("${mongo.database}") String mongoDatabase) throws Exception {

		log.debug("Connecting to mongo database with connection string:" + mongoConnectionString + " | database: "
				+ mongoDatabase);

		CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder()
						// https://jira.mongodb.org/browse/JAVA-2750
						.conventions(Arrays.asList(Conventions.ANNOTATION_CONVENTION)).automatic(true).build()));

		MongoClientSettings.Builder settings = MongoClientSettings.builder().codecRegistry(pojoCodecRegistry);
		settings.applyConnectionString(new ConnectionString(mongoConnectionString));

		mongoClient = MongoClients.create(settings.build());

		database = mongoClient.getDatabase(mongoDatabase);

		caseDefCollection = database.getCollection("caseDefinitions", JsonObject.class);
		caseInstCollection = database.getCollection("caseInstances", JsonObject.class);
		formCollection = database.getCollection("forms", JsonObject.class);
		recordTypeCollection = database.getCollection("recordType", JsonObject.class);

		bpmEngineCollection = database.getCollection("bpmEngine", JsonObject.class);

	}

	public MongoCollection<JsonObject> getCaseDefCollection() {
		return caseDefCollection;
	}

	public MongoCollection<JsonObject> getCaseInstCollection() {
		return caseInstCollection;
	}

	public MongoCollection<JsonObject> getFormCollection() {
		return formCollection;
	}

	public MongoCollection<JsonObject> getBpmEngineCollection() {
		return bpmEngineCollection;
	}

	public MongoCollection<JsonObject> getRecordTypeCollection() {
		return recordTypeCollection;
	}

	public MongoDatabase getDatabase() {
		return database;
	}

}
