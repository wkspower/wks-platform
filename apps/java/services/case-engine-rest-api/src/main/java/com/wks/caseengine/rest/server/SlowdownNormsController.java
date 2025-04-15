package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ShutdownNormsValueDTO;
import com.wks.caseengine.dto.SlowdownNormsValueDTO;
import com.wks.caseengine.service.ShutdownNormsService;
import com.wks.caseengine.service.SlowdownNormsService;

@RestController
@RequestMapping("task")
public class SlowdownNormsController {
	
	@Autowired
	private SlowdownNormsService slowdownNormsService;
	
	@GetMapping(value="/slowdownNorms")
	public List<SlowdownNormsValueDTO> getSlowdownNormsData(@RequestParam String year,@RequestParam String plantId){
		return	slowdownNormsService.getSlowdownNormsData(year, plantId);
	}
	
	@PostMapping(value="/slowdownNorms")
	public List<SlowdownNormsValueDTO> saveSlowdownNormsData(@RequestBody List<SlowdownNormsValueDTO> slowdownNormsValueDTOList){
		return	slowdownNormsService.saveSlowdownNormsData(slowdownNormsValueDTOList);
	}
	
	@GetMapping(value="/getSlowdownNormsSPData")
	public List<SlowdownNormsValueDTO> getSlowdownNormsSPData(@RequestParam String year,@RequestParam String plantId){
		return	slowdownNormsService.getSlowdownNormsSPData(year, plantId);
	}

}
