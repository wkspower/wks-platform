package com.wks.caseengine.dto;

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
public class WorkflowDTO {
	
	private String id;
   
    private String year;
    private String plantFkId;
    private String caseDefId;
    private String caseId;
    private String plantId;
    private String siteFKId;
    private String verticalFKId;
    private Boolean isDeleted;

   

}
