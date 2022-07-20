package com.mmc.bpm.engine.model.spi;

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
public class ProcessInstance {

	private String id;
	private String businessKey;

	private String definitionId;
	private String caseInstanceId;
	private Boolean ended;
	private Boolean suspended;
	private String tenantId;

}
