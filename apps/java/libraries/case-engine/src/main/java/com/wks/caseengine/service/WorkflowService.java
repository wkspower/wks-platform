package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;
import com.wks.caseengine.dto.WorkflowDTO;
import com.wks.caseengine.dto.WorkflowYearDTO;
import com.wks.caseengine.dto.WorkflowPageDTO;
import com.wks.caseengine.dto.WorkflowSubmitDTO;

public interface WorkflowService {
  
    public WorkflowPageDTO getCaseId(String year, String plantId, String siteId, String verticalId);
    public WorkflowDTO saveWorkFlow(WorkflowDTO workflowDTO);
    public Map<String, Object> getWorkFlow(String plantId,String year);
  	public Map<String, Object> getProductionAOPWorkflowData(String plantId,String year);
    public WorkflowDTO submitWorkflow(WorkflowSubmitDTO workflowSubmitDTO);

public int calculateExpressionWorkFlow(String year,String plantId);
    public void completeTaskWithComment(WorkflowSubmitDTO workflowSubmitDTO);
	 public WorkflowYearDTO saveWorkflowData( String plantId,List<WorkflowYearDTO> workflowYearDTO);
}
