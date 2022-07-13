package com.mmc.bpm.client.cases.instance;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import com.mmc.bpm.engine.model.spi.BusinessKey;
import com.mmc.bpm.engine.model.spi.ProcessInstance;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CaseInstance {

	private BusinessKey businessKey;
	private JsonObject attributes;

	@Builder.Default
	private List<ProcessInstance> processesInstances = new ArrayList<>();

	public void addProcessInstance(final ProcessInstance processInstance) {
		this.processesInstances.add(processInstance);
	}

}
