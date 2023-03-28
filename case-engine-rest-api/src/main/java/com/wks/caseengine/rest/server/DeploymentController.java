package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.bpm.engine.client.BpmEngineClientFacade;

@RestController
@RequestMapping("deployment")
public class DeploymentController {

	//TODO replace this hard code
	private static final String FILE_NAME_BPMN = "fileName.bpmn";
	
	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@PostMapping("/")
	public void deploy(@RequestBody String file) throws Exception {
		processEngineClient.deploy(FILE_NAME_BPMN, new String(file.getBytes()));
	}

}
