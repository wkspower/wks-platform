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
public class YearWiseContributionDataDTO {
	private String id;
	private Double prevYearActual;
	private Double PrevYearBudget;
	private Double CurrentYearBudget;
	private String remarks;


}
