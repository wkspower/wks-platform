package com.wks.caseengine.cases.definition.hook;

import java.util.List;

import com.wks.caseengine.cases.definition.action.CaseAction;

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
public class TaskCompleteHook {

	private String processDefKey;
	private String taskDefKey;

	private List<CaseAction> actions;

}
