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
public class FiveYearSummaryReportDTO {
	
	private String id;
	private Integer rowNo;
	private String material;
	private String uom;
    private Double price;
    private Double actualFourYearsAgo;
    private Double actualThreeYearsAgo;
    private Double actualTwoYearsAgo;
    private Double actualLastYear;
    private Double budgetCurrent;
    private String remark;

}
