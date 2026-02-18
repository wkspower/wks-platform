package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.ShutdownHistoryConfigDTO;
import com.wks.caseengine.dto.SlowdownHistoryConfigDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.PackagingConsumablesService;
import com.wks.caseengine.service.ShutdownHistoryService;

@RestController
@RequestMapping("task")
public class ShutdownHistoryController {
	
	@Autowired
	private ShutdownHistoryService shutdownHistoryService;
	
	@GetMapping(value="/shutdown-history")
	public AOPMessageVM getShutdownHistory(@RequestParam String plantId,@RequestParam String year){
		 return  shutdownHistoryService.getShutdownHistory(plantId,year);
	}
	
	@GetMapping(value="/type-of-sd")
	public AOPMessageVM getTypeOfSD(@RequestParam(required=false) String plantId,@RequestParam(required=false) String year){
		 return  shutdownHistoryService.getTypeOfSD(plantId,year);
	}
	
	@GetMapping(value="/line-details")
	public AOPMessageVM getLineDetails(@RequestParam(required=false) String plantId,@RequestParam(required=false) String year){
		 return  shutdownHistoryService.getLineDetails(plantId,year);
	}
	
	@PostMapping(value="/shutdown-history")
	public AOPMessageVM saveShutdownHistory(@RequestParam String year,@RequestParam String plantFKId, @RequestBody List<ShutdownHistoryConfigDTO> shutdownHistoryConfigDTOs) {
		return 	shutdownHistoryService.saveShutdownHistory(year,plantFKId,shutdownHistoryConfigDTOs);
	}
	
	@DeleteMapping(value="/shutdown-history")
	public AOPMessageVM deleteShutdownHistory(@RequestParam UUID id) {
		return 	shutdownHistoryService.deleteShutdownHistory(id);
	}

	@GetMapping(value = "/slowdown-history")
	public AOPMessageVM getSlowdownHistory(@RequestParam String plantId, @RequestParam String year) {
		return shutdownHistoryService.getSlowdownHistory(plantId, year);
	}

	@PostMapping(value = "/slowdown-history")
	public AOPMessageVM saveSlowdownHistory(
			@RequestParam String year,
			@RequestParam String plantFKId,
			@RequestBody List<SlowdownHistoryConfigDTO> slowdownHistoryConfigDTOs) {

		return shutdownHistoryService.saveSlowdownHistory(year, plantFKId, slowdownHistoryConfigDTOs);
	}

	@DeleteMapping(value = "/slowdown-history")
	public AOPMessageVM deleteSlowdownHistory(@RequestParam UUID id) {
		return shutdownHistoryService.deleteSlowdownHistory(id);
	}
}
