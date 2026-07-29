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

import org.bson.types.ObjectId;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Helpers for building the JSON documents the Mongo repositories insert.
 *
 * @author victor.franca
 */
public final class MongoDocuments {

	private MongoDocuments() {
	}

	/**
	 * Serialises {@code entity} to a document carrying an explicit {@code _id}.
	 *
	 * <p>
	 * The config repositories insert into
	 * {@code MongoCollection<org.bson.json.JsonObject>}, and {@code JsonObjectCodec}
	 * is not a {@code CollectibleCodec}: the driver neither generates nor tracks an
	 * {@code _id} for these documents, so
	 * {@link com.mongodb.client.result.InsertOneResult#getInsertedId()} returns null
	 * and the server assigns the id instead. Callers that dereferenced that result
	 * threw a {@link NullPointerException} <em>after</em> the write had already
	 * committed — the request failed with a 500 while the document existed, so
	 * retries silently piled up duplicates.
	 *
	 * <p>
	 * Supplying the id up front means the caller knows it without reading anything
	 * back, and is independent of driver-version codec behaviour.
	 *
	 * @param gson   the same Gson the repository uses, so serialisation is identical
	 * @param entity the domain object to persist
	 * @param id     the id to assign
	 * @return the document, as extended JSON with {@code _id} as an ObjectId
	 */
	public static org.bson.json.JsonObject withId(final Gson gson, final Object entity, final ObjectId id) {
		JsonObject document = gson.toJsonTree(entity).getAsJsonObject();

		JsonObject objectId = new JsonObject();
		objectId.addProperty("$oid", id.toHexString());
		document.add("_id", objectId);

		return new org.bson.json.JsonObject(gson.toJson(document));
	}

}
