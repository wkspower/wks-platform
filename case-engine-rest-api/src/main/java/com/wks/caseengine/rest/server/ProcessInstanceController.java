package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.caseengine.process.instance.ProcessInstanceService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("process-instance")
@Tag(name = "Process Instance", description = "Access information about processes instances in Camunda")
public class ProcessInstanceController {

	@Autowired
	private ProcessInstanceService processInstanceService;

	@GetMapping(value = "/{bpmEngineId}")
	public List<ProcessInstance> find(@PathVariable final String bpmEngineId,
			@RequestParam(required = false) String businessKey) throws Exception {
		return processInstanceService.find(Optional.ofNullable(businessKey), bpmEngineId);
	}

	@GetMapping(value = "/{bpmEngineId}/{id}/activity-instances")
	public List<ActivityInstance> getActivityInstances(@PathVariable final String bpmEngineId,
			@PathVariable final String id) throws Exception {
		return processInstanceService.getActivityInstances(id, bpmEngineId);
	}

}
