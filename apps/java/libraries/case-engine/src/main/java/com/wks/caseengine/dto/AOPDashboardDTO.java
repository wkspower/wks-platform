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
public class AOPDashboardDTO {
	
	private String id;
    private String siteId;
    private String verticalId;
    private String status;
    private String statusColor;
    private String statusTextColor;
    private Integer displayOrder;
    private Boolean isActive;
    private String notes;
    private String aopYear;
}
