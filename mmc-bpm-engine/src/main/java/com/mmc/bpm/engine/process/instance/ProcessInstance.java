package com.mmc.bpm.engine.process.instance;

import com.mmc.bpm.engine.cases.businesskey.BusinessKey;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class ProcessInstance {

	private BusinessKey businessKey;

}
