package com.mmc.bpm.client.tasks;

import java.util.List;

import com.mmc.bpm.engine.model.spi.Task;

public interface TaskService {

	public List<Task> find();

}
