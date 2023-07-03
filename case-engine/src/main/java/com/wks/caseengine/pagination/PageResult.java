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
package com.wks.caseengine.pagination;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Sort.Direction;

import com.google.gson.JsonObject;

public final class PageResult<T> {

	@SuppressWarnings("rawtypes")
	public static final PageResult EMPTY = new PageResult<>(null, false, false, null, null, Direction.ASC, 0);

	private List<T> content;
	private boolean hasNext;
	private boolean hasPrevious;
	private Object nextCursor;
	private Object prevCursor;

	public PageResult(List<T> content, boolean hasNext, boolean hasPrevious, Object nextCursor, Object prevCursor,
			Direction dir, int limit) {
		this.content = content;
		this.hasNext = hasNext;
		this.hasPrevious = hasPrevious;
		this.nextCursor = nextCursor;
		this.prevCursor = prevCursor;
	}

	public Integer size() {
		return content.size();
	}

	public List<T> content() {
		return content;
	}

	public T first() {
		return content.isEmpty() ? null : content.get(0);
	}

	public T last() {
		return content.isEmpty() ? null : content.get(size() - 1);
	}

	public boolean hasNext() {
		return hasNext;
	}

	public boolean hasPrevious() {
		return hasPrevious;
	}

	public String next() {
		return nextCursor != null ? nextCursor.toString() : null;
	}

	public String previous() {
		return prevCursor != null ? prevCursor.toString() : null;
	}

	@Override
	public String toString() {
		return "hasPrevious(" + hasPrevious + "), hasNext(" + hasNext + ")";
	}

	public Object toJson() {
		Map<String, Object> json = new HashMap<>();

		json.put("data", content());

		JsonObject paging = new JsonObject();
		paging.addProperty("hasPrevious", hasPrevious);
		paging.addProperty("hasNext", hasNext);

		JsonObject cursors = new JsonObject();
		cursors.addProperty("before", prevCursor != null ? prevCursor.toString() : "");
		cursors.addProperty("after", nextCursor != null ? nextCursor.toString() : "");
		paging.add("cursors", cursors);

		json.put("paging", paging);

		return json;
	}

}
