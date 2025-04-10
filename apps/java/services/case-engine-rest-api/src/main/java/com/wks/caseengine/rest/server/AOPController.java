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
import com.wks.caseengine.service.AOPService;

@RestController
@RequestMapping("task")
public class AOPController {
	
	@Autowired
	private AOPService aOPService;
	
	@GetMapping(value="/aop")
	public ResponseEntity<List<AOPDTO>> getAOP(@RequestParam String plantId,@RequestParam String year){
		 List<AOPDTO> aOPList= aOPService.getAOPData(plantId,year);
		 return ResponseEntity.ok(aOPList);
	}
	
	@PutMapping(value="/aop")
	public List<AOPDTO> updateAOP(@RequestBody List<AOPDTO> aOPDTOList) {
		aOPService.updateAOP(aOPDTOList);
		return aOPDTOList;
	}

    @GetMapping(value="/aop/calculate")
	public ResponseEntity<List<AOPDTO>> calculateData(@RequestParam String plantId,@RequestParam String year){
    	try {
    		 List<AOPDTO> aOPList= aOPService.calculateData(plantId,year);
    		 return ResponseEntity.ok(aOPList);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
		return null;
	}


}
