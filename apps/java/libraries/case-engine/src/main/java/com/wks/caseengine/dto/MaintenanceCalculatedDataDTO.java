package com.wks.caseengine.dto;

import lombok.*;

import java.util.UUID;

import org.springframework.context.annotation.Configuration;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MaintenanceCalculatedDataDTO {
    private String id;
    private Integer runningHoursInMonth;
    private Integer shoutdownHrs;
    private Integer nonShoutdownHrs;
    private Double avgSlowdownLoadPVT;
    private Integer slowdownLoadReduction;
    private Integer effectiveOperatingHrs;
    private Integer monthNo;
    private String aopYear;
    private String plantFKId;
}
