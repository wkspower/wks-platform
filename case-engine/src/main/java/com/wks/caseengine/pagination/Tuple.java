package com.wks.caseengine.pagination;

import java.util.List;

class Tuple<T> {

	private final List<T> content;
	private final String token;

	public Tuple(List<T> content, String token) {
		this.content = content;
		this.token = token;
	}
	
	public Tuple(List<T> content) {
		this(content, null);
	}

	public List<T> content() {
		return content;
	}

	public String token() {
		return token;
	}
	
}