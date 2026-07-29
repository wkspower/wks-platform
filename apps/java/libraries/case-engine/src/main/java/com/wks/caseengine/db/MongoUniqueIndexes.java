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

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import lombok.extern.slf4j.Slf4j;

/**
 * Lazily ensures the unique indexes that keep a config collection's natural key
 * unique.
 *
 * <p>
 * Databases here are resolved per tenant at request time, so there is no startup
 * point that knows the full set of tenants — the indexes have to be ensured on
 * first use instead. Each (database, collection, field) is attempted once per
 * process and then memoised, so this costs one round trip per tenant per
 * collection rather than one per request.
 *
 * @author victor.franca
 */
@Slf4j
class MongoUniqueIndexes {

	private final Set<String> attempted = ConcurrentHashMap.newKeySet();

	/**
	 * Creates a unique index on {@code field} if this process has not already tried
	 * to for this database and collection.
	 *
	 * <p>
	 * Enforcement is best-effort by design. A database that predates the index may
	 * already hold duplicates, and the create then fails; that tenant must still be
	 * able to use the engine, so the failure is logged rather than propagated. It is
	 * not retried until the next restart, which would otherwise add a failing round
	 * trip to every request.
	 */
	void ensure(final MongoDatabase database, final String collection, final String field) {
		if (!attempted.add(database.getName() + "." + collection + "." + field)) {
			return;
		}

		try {
			database.getCollection(collection).createIndex(Indexes.ascending(field),
					new IndexOptions().unique(true));
			log.debug("Ensured unique index {}.{} on '{}'", database.getName(), collection, field);
		} catch (RuntimeException e) {
			log.warn("Could not create unique index {}.{} on '{}' — existing duplicates must be removed before "
					+ "uniqueness can be enforced for this tenant. Cause: {}", database.getName(), collection, field,
					e.getMessage());
		}
	}

}
