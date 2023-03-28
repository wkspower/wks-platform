package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.bpm.engine.client.BpmEngineClientFacade;

@RestController
@RequestMapping("process-deployment")
public class ProcessDeploymentController {

	@Autowired
	private BpmEngineClientFacade processEngineClient;

	@PostMapping("/")
	public void deploy(@RequestParam(name = "file", required = true) MultipartFile file) throws Exception {
		processEngineClient.deploy(file.getName(), new String(file.getBytes()));
	}

}
