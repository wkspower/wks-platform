package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ConsumptionNormDTO;
import com.wks.caseengine.dto.ScreenDataConfigDTO;
import com.wks.caseengine.service.ScreenDataConfigService;

@RestController
@RequestMapping("task")
public class ScreenDataConfigController {
	
	@Autowired
	private ScreenDataConfigService screenDataConfigService;
	
	@GetMapping(value="/getScreenDataConfig")
	public ResponseEntity<ScreenDataConfigDTO> getScreenDataConfig(@RequestParam String verticalId,@RequestParam String screenName){
		ScreenDataConfigDTO screenDataConfigDTO	=screenDataConfigService.getScreenData(verticalId,screenName);
		return ResponseEntity.ok(screenDataConfigDTO);
	}

}
