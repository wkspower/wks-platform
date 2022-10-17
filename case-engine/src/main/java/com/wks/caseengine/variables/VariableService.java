package com.wks.caseengine.variables;

public interface VariableService {

	String findVariables(final String processInstanceId, final String bpmEngineId) throws Exception;

}
