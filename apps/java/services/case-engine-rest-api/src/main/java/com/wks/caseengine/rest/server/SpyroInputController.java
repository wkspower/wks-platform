package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.UUID;

import com.wks.caseengine.service.SpyroInputService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.SpyroInputDTO;

import com.wks.caseengine.message.vm.AOPMessageVM;

@RestController
@RequestMapping("task")
public class SpyroInputController {
	
	@Autowired
	private SpyroInputService spyroInputService;
	
	@GetMapping(value="/spyro-input")
	public AOPMessageVM getSpyroInputData(@RequestParam String year,@RequestParam String plantId,@RequestParam String Mode,@RequestParam String type){
		return	spyroInputService.getSpyroInputData(year, plantId,Mode, type);
	}

	@PostMapping(value="/spyro-input")
	public AOPMessageVM updateSpyroInputData(@RequestBody List<SpyroInputDTO> spyroInputDTOList,@RequestParam String year,@RequestParam String plantId){
		return spyroInputService.updateSpyroInputData(spyroInputDTOList,plantId,year);
	}

	@GetMapping(value = "/spyro-input-export-excel")
	public ResponseEntity<byte[]> exportConfigurationConstantsReport(
	         @RequestParam String year,@RequestParam String plantId,@RequestParam String mode
	        ) {
	    try {
			
	        byte[] excelBytes = spyroInputService.createExcel(year, plantId, mode, false, null);

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("SpyroInput.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}

	@PostMapping(value = "/spyro-input-import-excel", consumes = "multipart/form-data")
	public AOPMessageVM importExcel(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("mode") String mode,
			@RequestParam("file") MultipartFile file
	        ) {
			return	spyroInputService.importExcel(year, plantId, mode, file); 
	}


		
}
