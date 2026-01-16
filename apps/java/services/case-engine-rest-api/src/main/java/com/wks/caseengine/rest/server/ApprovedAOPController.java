package com.wks.caseengine.rest.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ApprovedAOPService;

@RestController
@RequestMapping("task")
public class ApprovedAOPController {
	
	@Autowired
	private ApprovedAOPService approvedAOPService;
	
	@PostMapping(value="/release-aop")
	public AOPMessageVM updateApprovedAOP(@RequestParam String plantId, @RequestParam String year) {
		return approvedAOPService.updateApprovedAOP(plantId,year);
	}
	
	@GetMapping(value="/release-aop")
	public AOPMessageVM getApprovedAOP(@RequestParam String plantId, @RequestParam String year) {
		return approvedAOPService.getApprovedAOP(plantId,year);
	}
	
	@DeleteMapping(value="/release-aop")
	public AOPMessageVM deleteApprovedAOP(@RequestParam String id) {
		return approvedAOPService.deleteApprovedAOP(id);
	}

}

