package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;
import com.wks.caseengine.service.ScreenMappingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.VerticalScreenMappingDTO;

@RestController
@RequestMapping("screen-mapping")
public class ScreenMappingController {
	
	@Autowired
	private ScreenMappingService screenMappingService;
	
	@GetMapping
	public	List<VerticalScreenMappingDTO> getScreenMapping(@RequestParam String verticalId){
		try {
			return screenMappingService.getScreenMapping(verticalId);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
