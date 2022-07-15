package com.mmc.bpm.client.cases.instance;

import java.util.ArrayList;
import java.util.List;

import com.mmc.bpm.engine.model.spi.BusinessKey;
import com.mmc.bpm.engine.model.spi.ProcessInstance;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class CaseInstance {

	@EqualsAndHashCode.Include
	private BusinessKey businessKey;

	private String attributes;

	@Builder.Default
	private List<ProcessInstance> processesInstances = new ArrayList<>();

	public void addProcessInstance(final ProcessInstance processInstance) {
		this.processesInstances.add(processInstance);
	}

}
