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
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.AOPMCCalculatedDataService;

@RestController
@RequestMapping("task")
public class AOPMCCalculatedDataController {

	@Autowired
	private AOPMCCalculatedDataService aOPMCCalculatedDataService;

	@GetMapping(value = "/getAOPMCCalculatedData")
	public AOPMessageVM getAOPMCCalculatedData(@RequestParam String plantId, @RequestParam String year) {
		return aOPMCCalculatedDataService.getAOPMCCalculatedData(plantId, year);
	}

	@PutMapping(value = "/editAOPMCCalculatedData")
	public List<AOPMCCalculatedDataDTO> editAOPMCCalculatedData(
			@RequestBody List<AOPMCCalculatedDataDTO> aOPMCCalculatedDataDTO,@RequestParam("plantId") String plantId,
			@RequestParam("year") String year) {
		return aOPMCCalculatedDataService.editAOPMCCalculatedData(aOPMCCalculatedDataDTO,false,plantId,year);

	}

	@GetMapping(value = "/getAOPMCCalculatedDataSP")
	public AOPMessageVM getAOPMCCalculatedDataSP(@RequestParam String plantId, @RequestParam String year) {
		return aOPMCCalculatedDataService.getAOPMCCalculatedDataSP(plantId, year);
	}

	@GetMapping(value = "/production-volume-data/export/excel")
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

	@PostMapping(value = "/production-volume-data/import/excel", consumes = "multipart/form-data")
	public AOPMessageVM importExcel(@RequestParam("plantId") String plantId,
			@RequestParam("year") String year, @RequestParam("file") MultipartFile file) {
		return aOPMCCalculatedDataService.importExcel(year, plantId, file);
		
	}

}
