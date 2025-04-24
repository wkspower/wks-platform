package com.wks.caseengine.rest.server;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.wks.caseengine.dto.WorkflowDTO;
import com.wks.caseengine.dto.WorkflowPageDTO;
import com.wks.caseengine.dto.WorkflowSubmitDTO;
import com.wks.caseengine.service.BusinessDemandDataService;
import com.wks.caseengine.service.WorkflowService;

@RestController
@RequestMapping("task")
public class WorkFlowController {


    @Autowired
	private WorkflowService workflowService;


    @GetMapping(value="/getCaseId")
	public	WorkflowPageDTO getCaseId(@RequestParam String year, @RequestParam String plantId, @RequestParam String siteId,@RequestParam String verticalId){
		System.out.println(plantId);
		return workflowService.getCaseId(year,plantId, siteId, verticalId);	
	}

    @PostMapping(value="/saveWorkflow")
	public 	WorkflowDTO saveWorkFlow(@RequestBody WorkflowDTO workflowDTO) {
		return workflowService.saveWorkFlow(workflowDTO);	
	}
    
    @GetMapping(value="/work-flow")
    public Map<String, Object> getWorkflowData( @RequestParam String plantId,@RequestParam String year){
    	Map<String, Object> data= workflowService.getWorkFlow(plantId,year);
    	return data;
    }
    
    @GetMapping(value="/production-aop/work-flow")
    public Map<String, Object> getProductionAOPWorkflowData( @RequestParam String plantId,@RequestParam String year){
    	Map<String, Object> data= workflowService.getProductionAOPWorkflowData(plantId,year);
    	return data;
    }
    

	@PostMapping(value="/submitWorkflow")
	public 	WorkflowDTO submitWorkflow(@RequestBody WorkflowSubmitDTO workflowSubmitDTO) {
		return workflowService.submitWorkflow(workflowSubmitDTO);	
	}


	@PostMapping(value="/completetask")
	public 	ResponseEntity<Void> completeTaskWithComment(@RequestBody WorkflowSubmitDTO workflowSubmitDTO) {
		 workflowService.completeTaskWithComment(workflowSubmitDTO);
		 return ResponseEntity.noContent().build();
		 
		 
	}






	
	
	
	


}
