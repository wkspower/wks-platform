package com.wks.caseengine.rest.model;

public class Users {

	private String userId;
	private char status;
	public Users(String email, char status) {
		this.userId = email;
        this.status = status;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public char getStatus() {
		return status;
	}
	public void setStatus(char status) {
		this.status = status;
	}
	
}
