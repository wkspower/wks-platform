package com.wks.caseengine.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.annotation.Configuration;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ReliabilityRecordDto {

    private UUID id;
    private String reportType;
    private String incidentDescription;
    private String rootCauseAnalysis;
    private String initiative;
    private String outcome;
    private String recommendation;
    private LocalDate targetDate;
    private String responsible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private String remarks;
    private String aopYear;
    private UUID plantId;
    private Integer rowNo;

}

