package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.bpm.BpmEngine;
import com.wks.caseengine.bpm.BpmEngineService;

@RestController
@RequestMapping("bpm-engine")
public class BpmEngineController {

	@Autowired
	private BpmEngineService bpmEngineService;

	@GetMapping(value = "/")
	public List<BpmEngine> find() throws Exception {
		return bpmEngineService.find();
	}
}
