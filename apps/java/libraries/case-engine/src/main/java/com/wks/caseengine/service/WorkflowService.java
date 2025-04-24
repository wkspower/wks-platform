package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;

import com.wks.caseengine.dto.WorkflowDTO;
import com.wks.caseengine.dto.WorkflowPageDTO;
import com.wks.caseengine.dto.WorkflowSubmitDTO;

public interface WorkflowService {
  
    public WorkflowPageDTO getCaseId(String year, String plantId, String siteId, String verticalId);
    public WorkflowDTO saveWorkFlow(WorkflowDTO workflowDTO);
    public Map<String, Object> getWorkFlow(String plantId,String year);
    public Map<String, Object> getProductionAOPWorkflowData(String plantId,String year);
    public WorkflowDTO submitWorkflow(WorkflowSubmitDTO workflowSubmitDTO);

    public void completeTaskWithComment(WorkflowSubmitDTO workflowSubmitDTO);
}
