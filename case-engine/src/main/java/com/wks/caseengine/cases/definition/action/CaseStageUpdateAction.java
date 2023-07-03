package com.wks.caseengine.cases.definition.action;

import com.wks.caseengine.cases.instance.CaseInstance;

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
public class CaseStageUpdateAction implements CaseAction {

	private String newStage;

	@Override
	public void apply(CaseInstance caseInstance) {
		caseInstance.setStage(newStage);
	}

}
