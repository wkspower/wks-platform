package com.mmc.automation.platform.casemanagement.cases.instance;

import com.google.gson.JsonObject;
import com.mmc.automation.platform.casemanagement.cases.businesskey.BusinessKey;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CaseInstance {

	private BusinessKey businessKey;
	private JsonObject attributes;

}
