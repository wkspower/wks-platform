package com.mmc.bpm.client.tasks;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mmc.bpm.engine.model.spi.Task;

public interface TaskService {

	public List<Task> find();

	public void claim(String taskId, String taskAssignee);

	public void unclaim(String taskId);

	public void complete(String taskId, JsonObject variables);

}
