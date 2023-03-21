package com.wks.caseengine.repository;

import org.bson.json.JsonObject;
import org.springframework.stereotype.Component;

import com.mongodb.client.FindIterable;

@Component
public class Paginator {

	private int page = 0;
	private int offset = 5;

	public FindIterable<JsonObject> apply(final FindIterable<JsonObject> findIterable) {
		return findIterable.skip(page > 0 ? ((page - 1) * offset) : 0).limit(offset);
	}
}
