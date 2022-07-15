package com.mmc.bpm.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mmc.bpm.client.cases.instance.CaseInstance;
import com.mmc.bpm.client.cases.instance.CaseInstanceService;

@RestController
public class CaseController {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@GetMapping(value = "/case")
	public List<CaseInstance> find() {
		return caseInstanceService.find();
	}

	@PostMapping(value = "/case")
	public CaseInstance save(@RequestBody String attributes) {
		return caseInstanceService.create(attributes);
	}

}
