package com.wks.caseengine.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ReliabilityPerformanceDto {

    private UUID id;
    private Integer rowNo;
    private String parameter;
    private String uom;
    private Double bestAchieved;
    private Double aop;
    private Double actual;
    private Double plann;
    private String limit;
    private String rationale;
    private Date createdAt;
    private Date updatedAt;
    private String updatedBy;
    private String remarks;
    private String aopYear;
    private UUID plantId;
    private String reportType;

}
