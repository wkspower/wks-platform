package com.mmc.bpm.client.tasks;

import java.util.List;

import com.mmc.bpm.engine.model.spi.Task;
import com.mmc.bpm.engine.model.spi.TaskAssignee;

public interface TaskService {

	public List<Task> find();

	public void claim(String taskId, TaskAssignee taskAssignee);

	public void unclaim(String taskId);

}
