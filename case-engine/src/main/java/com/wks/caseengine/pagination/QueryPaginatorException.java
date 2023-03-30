package com.wks.caseengine.pagination;

public class QueryPaginatorException extends RuntimeException {

	public QueryPaginatorException(String message) {
		super(message);
	}

	public QueryPaginatorException(String message, Exception ex) {
		super(message, ex);
	}

	private static final long serialVersionUID = 5032038649247198464L;
	
}
