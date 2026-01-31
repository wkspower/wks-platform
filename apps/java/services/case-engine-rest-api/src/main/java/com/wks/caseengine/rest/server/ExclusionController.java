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

import com.wks.caseengine.dto.ExclusionDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.ExclusionService;

@RestController
@RequestMapping("task")
public class ExclusionController {
	
	@Autowired 
	private ExclusionService exclusionService;
	
	@GetMapping(value="/exclusion-date")
	public AOPMessageVM getExclusionDate(@RequestParam String plantId,@RequestParam String year){
		 return  exclusionService.getExclusionDate(plantId,year);
	}
	
	@PostMapping(value="/exclusion-date")
	public AOPMessageVM saveExclusionDate(@RequestParam String year,@RequestParam String plantId, @RequestBody List<ExclusionDTO> exclusionDTOs) {
		return 	exclusionService.saveExclusionDate(year,plantId,exclusionDTOs);
	}
	
	@DeleteMapping(value="/exclusion-date")
	public AOPMessageVM deleteExclusionDate(@RequestParam String id){
		 return  exclusionService.deleteExclusionDate(id);
	}
	
	
	@GetMapping(value = "/exclusion-date-export")
	public ResponseEntity<byte[]> exportExclusionDate(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = exclusionService.exportExclusionDate(year,plantId,false,null); 

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("Exclusion_Date.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	@PostMapping(value = "/exclusion-date-import", consumes = "multipart/form-data")
	public AOPMessageVM importPlantTeam(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	exclusionService.importExclusionDate(year,UUID.fromString(plantId), file); 
	}
	
}
