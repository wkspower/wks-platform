package com.wks.caseengine.cases.instance;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class CaseInstanceFile {

	private String name;

	private String type;

	private String size;
	
	private String base64;
}
