package com.wks.caseengine.rest.server;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.dto.WorkflowDTO;
import com.wks.caseengine.service.BusinessDemandDataService;
import com.wks.caseengine.service.WorkflowService;

@RestController
@RequestMapping("task")
public class WorkFlowController {


    @Autowired
	private WorkflowService workflowService;


    @GetMapping(value="/getCaseId")
	public	List<WorkflowDTO> getCaseId(@RequestParam String year, @RequestParam String plantId, @RequestParam String siteId,@RequestParam String verticalId){
		System.out.println(plantId);
		return workflowService.getCaseId(year,plantId, siteId, verticalId);	
	}

    @PostMapping(value="/saveWorkflow")
	public 	WorkflowDTO saveWorkFlow(@RequestBody WorkflowDTO workflowDTO) {
		return workflowService.saveWorkFlow(workflowDTO);	
	}
    
    @GetMapping(value="/work-flow")
    public ResponseEntity<List<WorkflowDTO>> getWorkflowData( @RequestParam String plantId){
    	List<WorkflowDTO> data= workflowService.getWorkFlow(plantId);
    	return ResponseEntity.ok(data);
    }
    


	
	
	
	


}
