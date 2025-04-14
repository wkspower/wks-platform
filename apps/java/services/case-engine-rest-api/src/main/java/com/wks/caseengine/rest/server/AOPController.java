package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.AOPService;

@RestController
@RequestMapping("api/aop")
public class AOPController {
	
	@Autowired
	private AOPService aOPService;
	
	@GetMapping
	public ResponseEntity<AOPMessageVM> getAOP(@RequestParam String plantId,@RequestParam String year){
		AOPMessageVM response= aOPService.getAOPData(plantId,year);
		 return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@PutMapping
	public ResponseEntity<AOPMessageVM> updateAOP(@RequestBody List<AOPDTO> aOPDTOList) {
		AOPMessageVM response= aOPService.updateAOP(aOPDTOList);
		 return ResponseEntity.status(response.getCode()).body(response);
	}

    @GetMapping(value="/calculate")
	public ResponseEntity<AOPMessageVM> calculateData(@RequestParam String plantId,@RequestParam String year){
    	try {
    		AOPMessageVM response= aOPService.calculateData(plantId,year);
    		return ResponseEntity.status(response.getCode()).body(response);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
		return null;
	}


}
