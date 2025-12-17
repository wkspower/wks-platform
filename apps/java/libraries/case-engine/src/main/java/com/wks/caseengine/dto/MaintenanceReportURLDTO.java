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
public class MaintenanceReportURLDTO {

    private String id;
    private String reportCode;

    private String plantId;
    private String aopYear;
    private String reportURL;
    private Boolean isPlantWise;
    
}
