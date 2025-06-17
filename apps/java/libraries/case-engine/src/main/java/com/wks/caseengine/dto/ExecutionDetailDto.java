package com.wks.caseengine.dto;

import java.util.UUID;

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
public class ExecutionDetailDto {
	
	private UUID id;
    private UUID normParameterFKId;
    private String apr;
    private String remarks;
    private String auditYear;
    private String UOM; 
    private String plantId;

}
