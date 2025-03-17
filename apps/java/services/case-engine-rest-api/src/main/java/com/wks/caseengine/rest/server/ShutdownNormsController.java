package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.MCUNormsValueDTO;
import com.wks.caseengine.service.ShutdownNormsService;

@RestController
@RequestMapping("task")
public class ShutdownNormsController {
	
	@Autowired
	private ShutdownNormsService shutdownNormsService;
	
	@GetMapping(value="/getShutdownNormsData")
	public List<MCUNormsValueDTO> getShutdownNormsData(@RequestParam String year,@RequestParam String plantId){
		return	shutdownNormsService.getShutdownNormsData(year, plantId);
	}
	
	@PostMapping(value="/saveShutdownNormsData")
	public List<MCUNormsValueDTO> saveShutdownNormsData(@RequestBody List<MCUNormsValueDTO> mCUNormsValueDTOList){
		try {
			return	shutdownNormsService.saveShutdownNormsData(mCUNormsValueDTOList);
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
