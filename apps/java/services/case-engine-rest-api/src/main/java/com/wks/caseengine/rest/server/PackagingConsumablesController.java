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
import com.wks.caseengine.dto.PackagingAndConsumableTransactionDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.PackagingConsumablesService;

@RestController
@RequestMapping("task")
public class PackagingConsumablesController {
	
	@Autowired
	private PackagingConsumablesService packagingConsumablesService;
	
	@GetMapping(value="/packaging-consumables")
	public AOPMessageVM getPackagingConsumables(@RequestParam String plantId,@RequestParam String year){
		 return  packagingConsumablesService.getPackagingConsumables(plantId,year);
	}
	
	@GetMapping(value="/packaging-consumables-transaction")
	public AOPMessageVM getPackagingConsumablesTransaction(@RequestParam String plantId,@RequestParam String year){
		 return  packagingConsumablesService.getPackagingConsumablesTransaction(plantId,year);
	}
	
	@PostMapping(value="/packaging-consumables-transaction")
	public AOPMessageVM savePackagingConsumablesTransaction(@RequestParam String year,@RequestParam String plantId, @RequestBody List<PackagingAndConsumableTransactionDTO> packagingConsumablesTransactionDTOs) {
		return 	packagingConsumablesService.savePackagingConsumablesTransaction(year,plantId,packagingConsumablesTransactionDTOs);
	}
	
	@PostMapping(value="/packaging-consumables")
	public AOPMessageVM savePackagingConsumables(@RequestParam String year,@RequestParam String plantFKId, @RequestBody List<ConfigurationDTO> configurationDTOList) {
		return 	packagingConsumablesService.savePackagingConsumables(year,plantFKId,configurationDTOList);
	}
	
	@GetMapping(value = "/packaging-consumables-transaction-export")
	public ResponseEntity<byte[]> exportQualityTransaction(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = packagingConsumablesService.exportPackagingConsumablesTransaction(year,plantId,false,null); 

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("Packaging_Consumables_Transaction.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/packaging-consumables-transaction-import", consumes = "multipart/form-data")
	public AOPMessageVM importQualityTransaction(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	packagingConsumablesService.importPackagingConsumablesTransaction(year,UUID.fromString(plantId), file); 
	}

	
}
