package com.wks.caseengine.rest.security;

public class OpenPolicyResponse {

	private boolean result;

	public boolean getResult() {
		return this.result;
	}
	
	@Override
	public String toString() {
		return String.format("result: %s", result);
	}
	
}