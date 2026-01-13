package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.dto.PeopleInitiativeDTO;
import com.wks.caseengine.dto.PlantTeamDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.PeopleInitiativeService;

@RestController
@RequestMapping("task")
public class PeopleInitiativeController {
	
	@Autowired
	private PeopleInitiativeService peopleInitiativeService;
	
	@GetMapping(value="/plant-team")
	public AOPMessageVM getPlantTeam(@RequestParam String plantId,@RequestParam String year){
		 return  peopleInitiativeService.getPlantTeam(plantId,year);
	}
	
	@PostMapping(value="/plant-team")
	public AOPMessageVM savePlantTeam(@RequestParam String year,@RequestParam String plantId, @RequestBody List<PlantTeamDTO> plantTeamDTOs) {
		return 	peopleInitiativeService.savePlantTeam(year,plantId,plantTeamDTOs);
	}
	
	@GetMapping(value="/people-initiative")
	public AOPMessageVM getPeopleInitiative(@RequestParam String plantId,@RequestParam String year){
		 return  peopleInitiativeService.getPeopleInitiative(plantId,year);
	}
	
	@PostMapping(value="/people-initiative")
	public AOPMessageVM savePeopleInitiative(@RequestParam String year,@RequestParam String plantId, @RequestBody List<PeopleInitiativeDTO> peopleInitiativeDTOs) {
		return 	peopleInitiativeService.savePeopleInitiative(year,plantId,peopleInitiativeDTOs);
	}
	
}
