package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
@RequestMapping("task")
public class AOPController {
	
	@Autowired
	private AOPService aopService;
	
	@GetMapping(value="/getAOP")
	public ResponseEntity<List<AOPDTO>> getAOP(@RequestParam String plantId,@RequestParam String year){
		 List<AOPDTO> aOPList= aopService.getAOPData(plantId,year);
		 return ResponseEntity.ok(aOPList);
	}
	
	@PutMapping(value="/updateAOP")
	public List<AOPDTO> updateAOP(@RequestBody List<AOPDTO> aOPDTOList) {
		aopService.updateAOP(aOPDTOList);
		return aOPDTOList;
	}

    @GetMapping(value="/calculateData")
	public AOPMessageVM calculateData(@RequestParam String plantId,@RequestParam String year){
    	try {
    		 return aopService.calculateData(plantId,year);
    		// return ResponseEntity.ok(aOPList);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
		return null;
	}
    
    @GetMapping(value = "/aop-years")
    public ResponseEntity<List<Map<String, String>>> getYears() {
        List<Map<String, String>> data = aopService.getAOPYears();
        return ResponseEntity.ok(data);
    }

  
}
