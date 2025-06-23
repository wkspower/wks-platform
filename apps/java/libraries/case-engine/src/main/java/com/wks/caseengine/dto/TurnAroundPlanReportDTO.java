package com.wks.caseengine.dto;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class TurnAroundPlanReportDTO {
    private String id;
    private String remark;    
    private Integer rowNumber;
    private UUID plantFkId;
    private String aopYear;
    private String activity;
    private String toDate;
    private String fromDate;
    private Double durationInHrs;
    private Double periodInMonths;

}

