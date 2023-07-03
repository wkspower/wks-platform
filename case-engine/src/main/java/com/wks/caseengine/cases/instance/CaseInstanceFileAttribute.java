package com.wks.caseengine.cases.instance;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CaseInstanceFileAttribute {

	private String storage;

	private String name;

	private String url;

	private String type;

	private String originalName;

	private String hash;

	public CaseInstanceFileAttribute() {

	}

	public CaseInstanceFileAttribute(String storage, String name, String url, String type, String originalName) {
		this.storage = storage;
		this.name = name;
		this.url = url;
		this.type = type;
		this.originalName = originalName;
	}
}