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
public class WorkflowYearDTO {

	private String particulates;
	private String uom;
	private String fy202425AOP;
	private String fy202425Actual;
	private String fy202526AOP;
	private String remark;
	private String aopType;

}
