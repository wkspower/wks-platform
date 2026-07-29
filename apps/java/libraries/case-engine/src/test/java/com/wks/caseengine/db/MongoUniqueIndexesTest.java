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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mongodb.MongoCommandException;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

/**
 * @author victor.franca
 */
public class MongoUniqueIndexesTest {

	private MongoDatabase database;
	private MongoCollection<Document> collection;
	private MongoUniqueIndexes uniqueIndexes;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void setup() {
		database = mock(MongoDatabase.class);
		collection = mock(MongoCollection.class);
		when(database.getName()).thenReturn("tenant-a");
		when(database.getCollection("form")).thenReturn(collection);
		uniqueIndexes = new MongoUniqueIndexes();
	}

	@Test
	public void shouldCreateTheUniqueIndexOnFirstUse() {
		uniqueIndexes.ensure(database, "form", "key");

		verify(collection).createIndex(any(Bson.class), any(IndexOptions.class));
	}

	@Test
	public void shouldNotHitTheServerAgainForTheSameCollection() {
		uniqueIndexes.ensure(database, "form", "key");
		uniqueIndexes.ensure(database, "form", "key");
		uniqueIndexes.ensure(database, "form", "key");

		// Ensured once per process, not once per request.
		verify(collection, times(1)).createIndex(any(Bson.class), any(IndexOptions.class));
	}

	@Test
	public void shouldEnsurePerDatabaseSoEachTenantGetsTheIndex() {
		MongoDatabase other = mock(MongoDatabase.class);
		@SuppressWarnings("unchecked")
		MongoCollection<Document> otherCollection = mock(MongoCollection.class);
		when(other.getName()).thenReturn("tenant-b");
		when(other.getCollection("form")).thenReturn(otherCollection);

		uniqueIndexes.ensure(database, "form", "key");
		uniqueIndexes.ensure(other, "form", "key");

		verify(collection).createIndex(any(Bson.class), any(IndexOptions.class));
		verify(otherCollection).createIndex(any(Bson.class), any(IndexOptions.class));
	}

	@Test
	public void shouldNotPropagateAFailedIndexBuild() {
		doThrow(duplicateKeyOnBuild()).when(collection).createIndex(any(Bson.class), any(IndexOptions.class));

		// A tenant whose data predates the index still has to be able to use the
		// engine, so a failed build must not surface to the caller.
		uniqueIndexes.ensure(database, "form", "key");
	}

	@Test
	public void shouldNotRetryAFailedIndexBuildOnEveryCall() {
		doThrow(duplicateKeyOnBuild()).when(collection).createIndex(any(Bson.class), any(IndexOptions.class));

		uniqueIndexes.ensure(database, "form", "key");
		uniqueIndexes.ensure(database, "form", "key");

		// Otherwise every single request would pay for a round trip that cannot succeed.
		verify(collection, times(1)).createIndex(any(Bson.class), any(IndexOptions.class));
	}

	private MongoCommandException duplicateKeyOnBuild() {
		Document response = new Document("ok", 0).append("code", 11000).append("errmsg",
				"E11000 duplicate key error collection: tenant-a.form index: key_1");
		return new MongoCommandException(BsonDocumentOf(response), new ServerAddress());
	}

	private static org.bson.BsonDocument BsonDocumentOf(final Document document) {
		return document.toBsonDocument(org.bson.BsonDocument.class,
				com.mongodb.MongoClientSettings.getDefaultCodecRegistry());
	}

}
