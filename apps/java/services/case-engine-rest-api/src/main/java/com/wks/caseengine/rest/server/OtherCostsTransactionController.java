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

import com.wks.caseengine.dto.OtherCostsTransactionDto;
import com.wks.caseengine.dto.QualityTransactionDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.OtherCostsTransactionService;


@RestController
@RequestMapping("task")
public class OtherCostsTransactionController {
	
	@Autowired
	private OtherCostsTransactionService otherCostsTransactionService;
	
	@GetMapping(value="/other-costs-transaction")
	public AOPMessageVM getOtherCostsTransaction(@RequestParam String plantId,@RequestParam String year){
		 return  otherCostsTransactionService.getOtherCostsTransaction(plantId,year);
	}
	
	@PostMapping(value="/other-costs-transaction")
	public AOPMessageVM saveOtherCostsTransaction(@RequestParam String year,@RequestParam String plantId, @RequestBody List<OtherCostsTransactionDto> otherCostsTransactionDTOs) {
		return 	otherCostsTransactionService.saveOtherCostsTransaction(year,plantId,otherCostsTransactionDTOs);
	}
	
	
	@GetMapping(value = "/other-costs-transaction-export")
	public ResponseEntity<byte[]> exportQualityTransaction(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year
	        ) {
	    try {
			
	        byte[] excelBytes = otherCostsTransactionService.exportOtherCostsTransaction(year,plantId,false,null); 

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.parseMediaType(
	                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
	        headers.setContentDisposition(ContentDisposition.builder("attachment")
	                .filename("Other_Costs_Transaction.xlsx")
	                .build());
	        headers.setContentLength(excelBytes.length);

	        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
	    } catch (Exception e) {
	        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	}
	
	@PostMapping(value = "/other-costs-transaction-import", consumes = "multipart/form-data")
	public AOPMessageVM importQualityTransaction(
	         @RequestParam("plantId") String plantId,
            @RequestParam("year") String year,
			@RequestParam("file") MultipartFile file
	        ) {
			return	otherCostsTransactionService.importOtherCostsTransaction(year,UUID.fromString(plantId), file); 
	}
	
}
