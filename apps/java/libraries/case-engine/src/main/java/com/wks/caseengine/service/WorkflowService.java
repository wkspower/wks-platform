package com.wks.caseengine.service;

import java.util.List;

import com.wks.caseengine.dto.WorkflowDTO;

public interface WorkflowService {
  
    public List<WorkflowDTO> getCaseId(String year, String plantId, String siteId, String verticalId);
    public WorkflowDTO saveWorkFlow(WorkflowDTO workflowDTO);
    public List<WorkflowDTO> getWorkFlow(String plantId);
}
