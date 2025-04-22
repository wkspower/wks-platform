package com.wks.caseengine.dto;

import java.util.List;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.Switch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class WorkflowMasterDTO {

	private String id;
	private String casedefId;
	private String verticalFKId;
	private String workflowId;

    
    
	List<WorkflowStepsMasterDTO> steps;
	
}
