package com.mmc.bpm.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mmc.bpm.client.cases.instance.CaseInstance;
import com.mmc.bpm.client.cases.instance.CaseInstanceNotFoundException;
import com.mmc.bpm.client.cases.instance.CaseInstanceService;

@RestController
public class CaseController {

	@Autowired
	private CaseInstanceService caseInstanceService;

	@GetMapping(value = "/case")
	public List<CaseInstance> find() throws Exception {
		return caseInstanceService.find();
	}

	@GetMapping(value = "/case/{businessKey}")
	public CaseInstance get(@PathVariable String businessKey) throws Exception {
		return caseInstanceService.get(businessKey);
	}

	@PostMapping(value = "/case")
	public CaseInstance save(@RequestBody CaseInstance caseInstance) throws Exception {
		return caseInstanceService.create(caseInstance);
	}

	@DeleteMapping(value = "/case/{businessKey}")
	public void delete(@PathVariable String businessKey) throws Exception {
		try {
			caseInstanceService.delete(businessKey);
		} catch (CaseInstanceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Case Instance Not Found - " + businessKey, e);
		}
	}

}
