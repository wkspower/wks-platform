package com.mmc.bpm.engine.model.impl;

import com.mmc.bpm.engine.model.spi.ProcessInstance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author victor.franca
 *
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProcessInstanceImpl implements ProcessInstance {

	private String id;
	private String businessKey;

}
