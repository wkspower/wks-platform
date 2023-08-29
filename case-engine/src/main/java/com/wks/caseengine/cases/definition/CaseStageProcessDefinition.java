package com.wks.caseengine.cases.definition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class CaseStageProcessDefinition {
	
	public boolean autoStart;
	public String definitionKey;
	public String definitionName;

}
