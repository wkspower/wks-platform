package com.wks.caseengine.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.wks.bpm.engine.BpmEngine;
import com.wks.bpm.engine.client.BpmEngineClientFacade;
import com.wks.bpm.engine.model.spi.Task;
import com.wks.caseengine.repository.BpmEngineRepository;

@Component
public class TaskServiceImpl implements TaskService {

	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@Autowired
	private BpmEngineRepository bpmEngineRepository;

	@Override
	public List<Task> find(final String processInstanceBusinessKey, final Optional<String> bpmEngineId)
			throws Exception {

		if (bpmEngineId.isEmpty()) {
			List<BpmEngine> engines = bpmEngineRepository.find();

			List<Task> tasks = new ArrayList<>();
			engines.forEach(engine -> tasks
					.addAll(Arrays.asList(processEngineClient.findTasks(processInstanceBusinessKey, engine))));

			return tasks;
		} else {
			return Arrays.asList(processEngineClient.findTasks(processInstanceBusinessKey,
					bpmEngineRepository.get(bpmEngineId.get())));
		}
	}

	@Override
	public void claim(final String taskId, final String taskAssignee, final String bpmEngineId) throws Exception {
		processEngineClient.claimTask(taskId, taskAssignee, bpmEngineRepository.get(bpmEngineId));

	}

	@Override
	public void unclaim(final String taskId, final String bpmEngineId) throws Exception {
		processEngineClient.unclaimTask(taskId, bpmEngineRepository.get(bpmEngineId));
	}

	@Override
	public void complete(final String taskId, final JsonObject variables, final String bpmEngineId) throws Exception {
		processEngineClient.complete(taskId, variables, bpmEngineRepository.get(bpmEngineId));
	}

}
