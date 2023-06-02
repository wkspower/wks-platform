package com.wks.caseengine.tasks;

import java.util.List;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.model.spi.Task;

public interface TaskService {

	void create(final Task task) throws Exception;

	List<Task> find(final String processInstanceBusinessKey) throws Exception;

	void claim(final String taskId, final String taskAssignee) throws Exception;

	void unclaim(final String taskId) throws Exception;

	void complete(final String taskId, final JsonObject variables) throws Exception;

}
