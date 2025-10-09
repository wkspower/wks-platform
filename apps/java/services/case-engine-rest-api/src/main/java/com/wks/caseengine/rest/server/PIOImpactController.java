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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
	
	@PostMapping(value = "/pio-impact-import", consumes = "multipart/form-data")
	public AOPMessageVM importYieldExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	pioImpactService.importPIOImpact(year,UUID.fromString(plantId), file); 
	}
	
	@GetMapping(value = "/pio-impact-export")
	public ResponseEntity<byte[]> exportPIOImpact(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = pioImpactService.exportPIOImpact(year,plantId,false,null); //excelService.generateFlexibleExcel(data, plantId, year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId, year, reportType);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("PIO_IMPACT.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

		
}
