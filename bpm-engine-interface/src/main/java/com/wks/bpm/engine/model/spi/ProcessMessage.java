package com.wks.bpm.engine.model.spi;

import com.google.gson.JsonObject;

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
public class ProcessMessage {

	private String tenantId;
	
	private String messageName;
	private String businessKey;
	
	private JsonObject processVariables;


}
