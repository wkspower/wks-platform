package com.mmc.bpm.client.tasks;

import java.util.List;

import com.google.gson.JsonObject;
import com.mmc.bpm.engine.model.spi.Task;

public interface TaskService {

	public List<Task> find(final String processInstanceBusinessKey);

	public void claim(final String taskId, final String taskAssignee);

	public void unclaim(final String taskId);

	public void complete(final String taskId, final JsonObject variables);

}
