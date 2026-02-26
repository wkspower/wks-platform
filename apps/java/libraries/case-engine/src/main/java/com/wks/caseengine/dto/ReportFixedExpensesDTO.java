package com.wks.caseengine.dto;

import lombok.*;

import java.util.Date;

import org.springframework.context.annotation.Configuration;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Configuration
public class ReportFixedExpensesDTO {

    private String id;

    private String particulars;

    private Double fyPrevAOP;
    private Double fyPrevActual;
    private Double fyCurrAOP;
    private Double percentageChange;
    private Double variance;

    private String remarks;

    private String siteId;
    private String aopYear;
    private String updatedBy;

    private String saveStatus;
    private String errDescription;

    private Date updatedDate;
}
