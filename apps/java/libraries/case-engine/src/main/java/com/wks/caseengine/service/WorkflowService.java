package com.wks.caseengine.service;

import java.util.List;
import java.util.Map;

import com.wks.caseengine.dto.WorkflowDTO;

public interface WorkflowService {
  
    public List<WorkflowDTO> getCaseId(String year, String plantId, String siteId, String verticalId);
    public WorkflowDTO saveWorkFlow(WorkflowDTO workflowDTO);
    public Map<String, Object> getWorkFlow(String plantId,String year);
}
