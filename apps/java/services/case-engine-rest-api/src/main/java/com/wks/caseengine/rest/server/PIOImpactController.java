package com.wks.caseengine.rest.server;



import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.PIOImpactDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.PIOImpactService;

@RestController
@RequestMapping("task")
public class PIOImpactController {
	
	@Autowired
	private PIOImpactService pioImpactService;
		
	@GetMapping(value="/pio-impact")
	public AOPMessageVM getPIOImpact(@RequestParam String year,@RequestParam String plantId){
		return	pioImpactService.getPIOImpact(year, plantId);
	}
	
	@PostMapping(value="/pio-impact")
	public AOPMessageVM updatePIOImpact(@RequestParam String year,@RequestParam String plantId,@RequestBody List<PIOImpactDTO> pioImpactDTOs){
		return	pioImpactService.updatePIOImpact(year, plantId,pioImpactDTOs);
	}
	
	@DeleteMapping("/pio-impact")
    public AOPMessageVM deletPIOImpact(@RequestParam UUID id) {	
		return pioImpactService.deletePIOImpact(id);
    }
		
}
