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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wks.caseengine.dto.ConfigurationDTO;
import com.wks.caseengine.dto.PriceDifferentialTransactionDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.PackagingConsumablesService;
import com.wks.caseengine.service.PriceDifferentialService;

@RestController
@RequestMapping("task")
public class PriceDifferentialController {
	
	@Autowired
	private PriceDifferentialService priceDifferentialService;
	
	@GetMapping(value="/price-differential")
	public AOPMessageVM getPriceDifferential(@RequestParam String plantId,@RequestParam String year){
		 return  priceDifferentialService.getPriceDifferential(plantId,year);
	}
	
	@GetMapping(value="/price-differential-transaction")
	public AOPMessageVM getPriceDifferentialTransaction(@RequestParam String plantId,@RequestParam String year){
		 return  priceDifferentialService.getPriceDifferentialTransaction(plantId,year);
	}
	
	@PostMapping(value="/price-differential")
	public AOPMessageVM savePriceDifferential(@RequestParam String year,@RequestParam String plantFKId, @RequestBody List<ConfigurationDTO> configurationDTOList) {
		return 	priceDifferentialService.savePriceDifferential(year,plantFKId,configurationDTOList);
	}
	
	@PostMapping(value="/price-differential-transaction")
	public AOPMessageVM savePriceDifferentialTransaction(@RequestParam String year,@RequestParam String plantFKId, @RequestBody List<PriceDifferentialTransactionDTO> priceDifferentialTransactionDTO) {
		return 	priceDifferentialService.savePriceDifferentialTransaction(year,plantFKId,priceDifferentialTransactionDTO);
	}
	
	@GetMapping(value = "/price-differential-transaction-export")
	public ResponseEntity<byte[]> exportQualityTransaction(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = priceDifferentialService.exportPriceDifferentialTransaction(year,plantId,false,null); 

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("Price_Differential_Service.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/price-differential-transaction-import", consumes = "multipart/form-data")
	public AOPMessageVM importPriceDifferentialTransaction(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	priceDifferentialService.importPriceDifferentialTransaction(year,UUID.fromString(plantId), file); 
	}
	
	

	
}

