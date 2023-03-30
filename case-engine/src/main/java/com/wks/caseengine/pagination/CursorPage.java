package com.wks.caseengine.pagination;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.JsonObject;

public class CursorPage<T> {
	
	private Optional<List<T>> data;
	private Optional<Integer> limit;
	private Cursor currentToken;
	private Cursor previousToken;
	
	public CursorPage(List<T> data, Cursor previousToken, Cursor nextToken, int limit) {
		super();
		this.previousToken = previousToken;
		this.currentToken = nextToken;
		this.data = Optional.ofNullable(data);
		this.limit = Optional.ofNullable(limit);
	}

	public List<T> getData() {
		return data.orElse(Collections.emptyList());
	}
	
	public boolean hasNext() {
		return currentToken.hasToken();
	}
	
	public int limit() {
		return limit.orElse(5);
	}
	
	public Cursor currentToken() {
		return currentToken;
	}
	
	public Cursor previousToken() {
		return previousToken;
	}
	
	public int getSize() {
		return getData().size();
	}
	
	public T get(int index) {
		if (getData().isEmpty()) {
			return null;
		}
		
		return getData().get(index);
	}
	
	public Object toJson() {
		Map<String, Object> json = new HashMap<>();
		
		json.put("data", getData());
		
		JsonObject paging = new JsonObject();
		paging.addProperty("previousToken", previousToken.token());
		paging.addProperty("nextToken", currentToken.token());
		paging.addProperty("sort", currentToken.sort());
		paging.addProperty("dir", currentToken.dir());
		paging.addProperty("limit", limit());
		paging.addProperty("hasNext", hasNext());
		json.put("paging", paging);
		
		return json;
	}

}

