package com.wks.caseengine.cases.instance;

import java.io.Serializable;

public enum CaseAttributeType implements Serializable {

	STRING("String"), JSON("Json");

	private String value;

	private CaseAttributeType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
