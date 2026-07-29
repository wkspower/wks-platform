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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bson.BsonDocument;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wks.caseengine.form.Form;

/**
 * @author victor.franca
 */
public class MongoDocumentsTest {

	private final Gson gson = new Gson();

	@Test
	public void shouldCarryTheSuppliedIdAsAnObjectId() {
		ObjectId id = new ObjectId();

		BsonDocument document = BsonDocument.parse(MongoDocuments.withId(gson, form(), id).getJson());

		assertTrue(document.get("_id").isObjectId(), "_id must be an ObjectId, not a string");
		assertEquals(id, document.get("_id").asObjectId().getValue());
	}

	@Test
	public void shouldPreserveTheEntityFields() {
		BsonDocument document = BsonDocument.parse(MongoDocuments.withId(gson, form(), new ObjectId()).getJson());

		assertEquals("customer-support", document.getString("key").getValue());
		assertEquals("Customer Support", document.getString("title").getValue());
	}

	@Test
	public void shouldPreserveNestedJsonStructure() {
		Form form = form();
		JsonObject structure = new JsonObject();
		structure.addProperty("display", "form");
		form.setStructure(structure);

		BsonDocument document = BsonDocument.parse(MongoDocuments.withId(gson, form, new ObjectId()).getJson());

		// The builder schema must land as a nested document, not a stringified blob.
		assertTrue(document.get("structure").isDocument());
		assertEquals("form", document.getDocument("structure").getString("display").getValue());
	}

	private Form form() {
		Form form = new Form();
		form.setKey("customer-support");
		form.setTitle("Customer Support");
		return form;
	}

}
