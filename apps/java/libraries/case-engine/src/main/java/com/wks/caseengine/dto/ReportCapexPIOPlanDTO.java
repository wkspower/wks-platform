package com.wks.caseengine.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Configuration
public class ReportCapexPIOPlanDTO {

    private String id;

    private String proposal;
    private String category;
    private String justification;

    private Double costRsCr;
    private Double benefitRsCr;

    private String targetPlan;
    private String statusPlan;
    private String remarks;

    private String siteId;
    private String aopYear;
    private String updatedBy;

    private String saveStatus;
    private String errDescription;

    private Date updatedDate;
}
