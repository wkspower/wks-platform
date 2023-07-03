package com.wks.storage.exception;

public class StorageException extends RuntimeException {

	private static final long serialVersionUID = -3644218680986166831L;

	public StorageException(Exception e) {
		super(e);
	}

}
