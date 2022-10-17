package com.wks.caseengine.tasks;

import java.util.List;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.model.spi.Task;

public interface TaskService {

	public List<Task> find(final String processInstanceBusinessKey, final String bpmEngineId) throws Exception;

	public void claim(final String taskId, final String taskAssignee, final String bpmEngineId) throws Exception;

	public void unclaim(final String taskId, final String bpmEngineId) throws Exception;

	public void complete(final String taskId, final JsonObject variables, final String bpmEngineId) throws Exception;

}
