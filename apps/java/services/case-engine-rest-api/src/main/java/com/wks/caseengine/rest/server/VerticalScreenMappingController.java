package com.wks.caseengine.rest.server;

import java.util.Map;
import com.wks.caseengine.service.VerticalScreenMappingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("task/screen-mapping")
public class VerticalScreenMappingController {
	
	@Autowired
	private VerticalScreenMappingService verticalScreenMappingService;
	
	@GetMapping
	public Map<String, Object> getVerticalScreenMapping(@RequestParam String verticalId) throws Exception{
			return verticalScreenMappingService.getVerticalScreenMapping(verticalId);
	}

}
