package com.wks.caseengine.rest.server;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wks.caseengine.message.vm.AOPMessageVM;
import com.wks.caseengine.service.BasisReportService;

@RestController
@RequestMapping("task")
public class BasisReportController {
	
	@Autowired
	private BasisReportService basisReportService;
	
	@GetMapping(value="/report/norms-basis/pe")
	public ResponseEntity<AOPMessageVM> getNormBasisReport(@RequestParam String plantId,@RequestParam String year,@RequestParam String type,@RequestParam(value="periodFrom", required=false) String periodFrom,@RequestParam(value="periodTo", required=false) String periodTo){
		AOPMessageVM response	=basisReportService.getNormBasisReportForPE(plantId,year,type,periodFrom,periodTo);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	@GetMapping(value="/report/norms-basis/mode")
	public ResponseEntity<AOPMessageVM> getNormBasisReportCracker(@RequestParam String plantId,@RequestParam String year,@RequestParam(value="type", required=false) String type,@RequestParam(value="mode", required=false) String mode){
		AOPMessageVM response	=basisReportService.getNormBasisReportCracker(plantId,year,type,mode);
		return ResponseEntity.status(response.getCode()).body(response);
	}
	
	
}
