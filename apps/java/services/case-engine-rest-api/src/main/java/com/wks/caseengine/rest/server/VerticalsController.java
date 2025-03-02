package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.dto.VerticalsDTO;
import com.wks.caseengine.service.VerticalsService;

@RestController
@RequestMapping("task")
public class VerticalsController {
	
	@Autowired
	private VerticalsService verticalsService;
	
	@GetMapping(value="/getAllVerticals")
	public List<VerticalsDTO> getAllVerticals() {
		return verticalsService.getAllVerticals();
	}

}
