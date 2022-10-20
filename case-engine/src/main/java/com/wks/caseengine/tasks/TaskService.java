package com.wks.caseengine.tasks;

import java.util.List;
import java.util.Optional;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.model.spi.Task;

public interface TaskService {

	List<Task> find(final String processInstanceBusinessKey, final Optional<String> bpmEngineId) throws Exception;

	void claim(final String taskId, final String taskAssignee, final String bpmEngineId) throws Exception;

	void unclaim(final String taskId, final String bpmEngineId) throws Exception;

	void complete(final String taskId, final JsonObject variables, final String bpmEngineId) throws Exception;

}
