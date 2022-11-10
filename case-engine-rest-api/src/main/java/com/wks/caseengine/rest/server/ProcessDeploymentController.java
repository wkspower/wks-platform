package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.bpm.engine.client.BpmEngineClientFacade;
import com.wks.caseengine.bpm.BpmEngineService;

@RestController
@RequestMapping("process-deployment")
public class ProcessDeploymentController {

	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@Autowired
	private BpmEngineService bpmEngineService;

	@PostMapping("/create/{bpmEngineId}")
	public void deploy(@PathVariable final String bpmEngineId,
			@RequestParam(name = "file", required = true) MultipartFile file) throws Exception {
		processEngineClient.deploy(bpmEngineService.get(bpmEngineId), file.getName(), new String(file.getBytes()));
	}

}
