package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.dto.AOPDTO;
import com.wks.caseengine.service.AOPService;

@RestController
@RequestMapping("task")
public class AOPController {
	
	@Autowired
	private AOPService aOPService;
	
	@GetMapping(value="/getAOP")
	public ResponseEntity<List<AOPDTO>> getAOP(){
		 List<AOPDTO> aOPList= aOPService.getAOP();
		 return ResponseEntity.ok(aOPList);
	}
	
	@PutMapping(value="/updateAOP")
	public AOPDTO updateAOP(AOPDTO aOPDTO) {
		aOPService.updateAOP(aOPDTO);
		return aOPDTO;
	}

}
