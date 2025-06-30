package com.wks.caseengine.dto;

import java.util.Date;
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
public class AnnualProductionPlanReportDto {
	
	private UUID id;
    private Integer rowNo;
    private String activity;
    private Date periodFrom;
    private Date periodTo;
    private Double rateValue;
    private Double durationHours;
    private String maxHourlyRateValue;
    private String uom;
    private String remark;
    private UUID plantFkId;
    private String aopYear;
    private String reportType;

}
