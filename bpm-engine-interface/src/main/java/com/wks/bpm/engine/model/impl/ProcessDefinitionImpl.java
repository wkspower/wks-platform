package com.wks.bpm.engine.model.impl;

import com.wks.bpm.engine.model.spi.ProcessDefinition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author victor.franca
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDefinitionImpl implements ProcessDefinition {

	private String bpmEngineId;
	
	private String id;
	
	private String key;
	
	private String name;

	private String bpmn20Xml;

}
