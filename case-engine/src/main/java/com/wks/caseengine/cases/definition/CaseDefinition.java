package com.wks.caseengine.cases.definition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class CaseDefinition {

	private String id;

	private String name;

	private String formKey;

	private String stagesLifecycleProcessKey;

}
