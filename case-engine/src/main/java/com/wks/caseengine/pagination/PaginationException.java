package com.wks.caseengine.pagination;

public class PaginationException extends RuntimeException {

	private static final long serialVersionUID = 5032038649247198464L;
	
	public PaginationException(String message) {
		super(message);
	}

	public PaginationException(String message, Exception ex) {
		super(message, ex);
	}

}
