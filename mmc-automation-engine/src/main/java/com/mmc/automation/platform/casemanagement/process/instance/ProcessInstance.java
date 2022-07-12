package com.mmc.automation.platform.casemanagement.process.instance;

import com.mmc.automation.platform.casemanagement.cases.businesskey.BusinessKey;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ProcessInstance {

	private BusinessKey businessKey;

}
