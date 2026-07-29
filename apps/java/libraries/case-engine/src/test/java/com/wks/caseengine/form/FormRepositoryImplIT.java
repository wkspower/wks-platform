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
package com.wks.caseengine.form;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bson.BsonDocument;
import org.bson.json.JsonObject;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.gson.GsonBuilder;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

/**
 * Exercises the real write path against an embedded MongoDB.
 *
 * <p>
 * The bug these cover shipped because nothing drove {@code save()} against a
 * real server: {@code InsertOneResult#getInsertedId()} is null for a
 * {@code MongoCollection<JsonObject>}, so the old implementation threw a
 * {@link NullPointerException} <em>after</em> committing the write. Only a real
 * insert reproduces that.
 *
 * @author victor.franca
 */
@DataMongoTest
@ExtendWith(SpringExtension.class)
public class FormRepositoryImplIT {

	@Autowired
	private MongoOperations operations;

	private FormRepositoryImpl repository;

	@BeforeEach
	public void setup() {
		collection().drop();

		repository = new FormRepositoryImpl() {
			@Override
			protected MongoCollection<JsonObject> getCollection() {
				return collection();
			}
		};
		ReflectionTestUtils.setField(repository, "gsonBuilder", new GsonBuilder());
	}

	private MongoCollection<JsonObject> collection() {
		return ((MongoTemplate) operations).getDb().getCollection("form", JsonObject.class);
	}

	@Test
	public void shouldReturnTheIdOfTheInsertedForm() {
		String id = repository.save(form("customer-support", "Customer Support"));

		assertNotNull(id, "save must return the new document id, not throw");
		assertTrue(ObjectId.isValid(id), "returned id must be a valid ObjectId: " + id);
	}

	@Test
	public void shouldPersistTheFormUnderTheReturnedId() {
		String id = repository.save(form("customer-support", "Customer Support"));

		JsonObject stored = collection().find(Filters.eq("_id", new ObjectId(id))).first();

		assertNotNull(stored, "the returned id must address the document actually written");

		BsonDocument document = BsonDocument.parse(stored.getJson());
		assertEquals("customer-support", document.getString("key").getValue());
		assertEquals("Customer Support", document.getString("title").getValue());
	}

	@Test
	public void shouldRoundTripTheSavedFormThroughGet() throws Exception {
		repository.save(form("customer-support", "Customer Support"));

		Form reloaded = repository.get("customer-support");

		assertEquals("customer-support", reloaded.getKey());
		assertEquals("Customer Support", reloaded.getTitle());
	}

	@Test
	public void shouldWriteExactlyOneDocumentPerSave() {
		repository.save(form("a", "A"));
		repository.save(form("b", "B"));

		assertEquals(2, collection().countDocuments());
	}

	@Test
	public void shouldRejectDuplicateKeyOnceTheUniqueIndexExists() {
		collection().createIndex(Indexes.ascending("key"), new IndexOptions().unique(true));

		repository.save(form("customer-support", "Customer Support"));

		// The duplicate must be refused by the database rather than quietly stored —
		// this is what stopped retries piling up rows.
		assertThrows(MongoWriteException.class, () -> repository.save(form("customer-support", "Again")));
		assertEquals(1, collection().countDocuments());
	}

	private Form form(final String key, final String title) {
		Form form = new Form();
		form.setKey(key);
		form.setTitle(title);
		return form;
	}

}
