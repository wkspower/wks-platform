package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.model.spi.ActivityInstance;
import com.wks.bpm.engine.model.spi.ProcessInstance;
import com.wks.caseengine.process.instance.ProcessInstanceService;

@RestController
public class ProcessInstanceController {

	@Autowired
	private ProcessInstanceService processInstanceService;

	@GetMapping(value = "/process-instance")
	public List<ProcessInstance> find(@RequestParam(required = false) String businessKey) {
		return processInstanceService.find(Optional.ofNullable(businessKey));
	}

	@GetMapping(value = "/process-instance/{id}/activity-instances")
	public List<ActivityInstance> getActivityInstances(@PathVariable String id) {
		return processInstanceService.getActivityInstances(id);
	}

}
