package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
	
	@DeleteMapping(value="/plant-team")
	public AOPMessageVM deletePlantTeam(@RequestParam String id){
		 return  peopleInitiativeService.deletePlantTeam(id);
	}
	
	@PostMapping(value="/plant-team")
	public AOPMessageVM savePlantTeam(@RequestParam String year,@RequestParam String plantId, @RequestBody List<PlantTeamDTO> plantTeamDTOs) {
		return 	peopleInitiativeService.savePlantTeam(year,plantId,plantTeamDTOs);
	}
	
	@GetMapping(value="/people-initiative")
	public AOPMessageVM getPeopleInitiative(@RequestParam String plantId,@RequestParam String year){
		 return  peopleInitiativeService.getPeopleInitiative(plantId,year);
	}
	
	@DeleteMapping(value="/people-initiative")
	public AOPMessageVM deletePeopleInitiative(@RequestParam String id){
		 return  peopleInitiativeService.deletePeopleInitiative(id);
	}
	
	@PostMapping(value="/people-initiative")
	public AOPMessageVM savePeopleInitiative(@RequestParam String year,@RequestParam String plantId, @RequestBody List<PeopleInitiativeDTO> peopleInitiativeDTOs) {
		return 	peopleInitiativeService.savePeopleInitiative(year,plantId,peopleInitiativeDTOs);
	}
	
	@GetMapping(value = "/people-initiative-export")
	public ResponseEntity<byte[]> exportYieldReport(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = peopleInitiativeService.exportPeopleInitiative(year,plantId,false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("People_Initiative.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	@PostMapping(value = "/people-initiative-import", consumes = "multipart/form-data")
	public AOPMessageVM importPeopleInitiative(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	peopleInitiativeService.importPeopleInitiative(year,UUID.fromString(plantId), file); 
	}

	@GetMapping(value = "/plant-team-export")
	public ResponseEntity<byte[]> exportPlantTeam(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = peopleInitiativeService.exportPlantTeam(year,plantId,false,null); 

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("plant_team.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	@PostMapping(value = "/plant-team-import", consumes = "multipart/form-data")
	public AOPMessageVM importPlantTeam(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	peopleInitiativeService.importPlantTeam(year,UUID.fromString(plantId), file); 
	}
	
}
