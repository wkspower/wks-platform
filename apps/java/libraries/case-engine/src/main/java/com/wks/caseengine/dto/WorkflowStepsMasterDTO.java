package com.wks.caseengine.dto;


import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WorkflowStepsMasterDTO {
	private String id;
	private String workflowMasterFKId;
	private String name;
    private String displayName;
    private Integer sequence;
    private Boolean isRemarksDisabled;
    private String status;
  
	
}
