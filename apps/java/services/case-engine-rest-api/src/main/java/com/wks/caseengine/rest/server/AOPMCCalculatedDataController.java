package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.AOPMCCalculatedDataDTO;
import com.wks.caseengine.entity.Plants;
import com.wks.caseengine.entity.Sites;
import com.wks.caseengine.entity.Verticals;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.repository.PlantsRepository;
import com.wks.caseengine.repository.SiteRepository;
import com.wks.caseengine.repository.VerticalsRepository;
import com.wks.caseengine.service.AOPMCCalculatedDataService;

@RestController
@RequestMapping("task")
public class AOPMCCalculatedDataController {

	@Autowired
	private AOPMCCalculatedDataService aOPMCCalculatedDataService;
	
	@Autowired
	private PlantsRepository plantsRepository;

	@Autowired
	private VerticalsRepository verticalRepository;
	
	@Autowired
	private SiteRepository siteRepository;

	@GetMapping(value = "/production-target")
	public AOPMessageVM getAOPMCCalculatedData(@RequestParam String plantId, @RequestParam String year) {
		return aOPMCCalculatedDataService.getAOPMCCalculatedData(plantId, year);
	}
	
	@GetMapping(value = "/production-target-line")
	public AOPMessageVM getProductionTarget(@RequestParam String plantId, @RequestParam String year,@RequestParam(required=false) String lineId) {
		return aOPMCCalculatedDataService.getProductionTarget(plantId, year,lineId);
	}
	
	@GetMapping(value = "/max-achieved-capacity")
	public AOPMessageVM getMaxAchievedCapacity(@RequestParam String plantId, @RequestParam String year) {
		return aOPMCCalculatedDataService.getMaxAchievedCapacity(plantId, year);
	}
	
	@PostMapping(value = "/max-achieved-capacity")
	public AOPMessageVM updateMaxAchievedCapacity(@RequestParam String plantId, @RequestParam String year,@RequestBody List<AOPMCCalculatedDataDTO> aopMCCalculatedDataDTOs) {
		return aOPMCCalculatedDataService.updateMaxAchievedCapacity(plantId, year,aopMCCalculatedDataDTOs);
	}
	
	@GetMapping(value = "/design-capacity")
	public AOPMessageVM getDesignCapacity(@RequestParam String plantId, @RequestParam String year) {
		return aOPMCCalculatedDataService.getDesignCapacity(plantId, year);
	}
	
	@GetMapping(value = "/production-target-export-excel")
	public ResponseEntity<byte[]> exportProductionTarget(
			@RequestParam String plantId, @RequestParam String year) {
	    try {
			
	        byte[] excelBytes = aOPMCCalculatedDataService.exportProductionTarget(year, plantId, false, null);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("Production_Target.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/design-capacity")
	public AOPMessageVM updateDesignCapacity(@RequestParam String plantId,@RequestParam String year, @RequestBody List<AOPMCCalculatedDataDTO> aopMCCalculatedDataDTOList) {
		return aOPMCCalculatedDataService.updateDesignCapacity(plantId,year, aopMCCalculatedDataDTOList);
	}

	@PutMapping(value = "/production-target")
	public List<AOPMCCalculatedDataDTO> editAOPMCCalculatedData(
			@RequestBody List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTO, @RequestParam("plantId") String plantId,
			@RequestParam("year") String year) {
		return aOPMCCalculatedDataService.editAOPMCCalculatedData(aOPMCCalculatedDataDTO, false, year, plantId);

	}

	@GetMapping(value = "/calculate-production-target")
	public AOPMessageVM getAOPMCCalculatedDataSP(@RequestParam String plantId, @RequestParam String year) {
		return aOPMCCalculatedDataService.getAOPMCCalculatedDataSP(plantId, year);
	}

	@GetMapping(value = "/production-target-export")
	public ResponseEntity<byte[]> exportProductionVolumeDataReport(@RequestParam("plantId") String plantId,
			@RequestParam("year") String year) {
		try {

			byte[] excelBytes = aOPMCCalculatedDataService.createExcel(year, plantId, false, null); // excelService.generateFlexibleExcel(data,
																									// plantId,
																									// year);//productionVolumeDataReportExportService.getReportForPlantProductionPlanData(plantId,
																									// year,
																									// reportType);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(
					MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
			headers.setContentDisposition(
					ContentDisposition.builder("attachment").filename("Production Volume Data.xlsx").build());
			headers.setContentLength(excelBytes.length);

			return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping(value = "/production-target-import", consumes = "multipart/form-data")
	public AOPMessageVM importExcel(@RequestParam("plantId") String plantId,
			@RequestParam("year") String year, @RequestParam("file") MultipartFile file) {
		Plants plant = plantsRepository.findById(UUID.fromString(plantId))
                .orElseThrow(() -> new IllegalArgumentException("Invalid plant ID"));
        Verticals vertical = verticalRepository.findById(plant.getVerticalFKId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid vertical ID"));
        Sites site = siteRepository.findById(plant.getSiteFkId()).get();
        boolean pvc= vertical.getName().equalsIgnoreCase("PVC") && site.getName().equalsIgnoreCase("VMD");
        if(vertical.getName().equalsIgnoreCase("PE") || vertical.getName().equalsIgnoreCase("PP") || vertical.getName().equalsIgnoreCase("PET") || pvc) {
        	return aOPMCCalculatedDataService.importExcelPE(year, plantId, file);
        }else {
        	return aOPMCCalculatedDataService.importExcel(year, plantId, file);
        }
		

	}

}
