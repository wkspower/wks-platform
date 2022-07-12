package com.mmc.bpm.engine.camunda.model;

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
public class CamundaProcessInstance implements ProcessInstance {

	private String id;

}
