package com.wks.caseengine.dto;

import java.util.Date;

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

public class ShutdownSlowdownPlanDTO {
    private String id;
    private String plant;
    private Double noOfShutdownDays;
    private Double noOfSlowdownDays;
    private String monthPlan;
    private String shutdownSlowdownPlan;
    private String remarks;
    private String siteId;
    private String aopYear;
    private String updatedBy;
    private java.util.Date updatedDate;
}