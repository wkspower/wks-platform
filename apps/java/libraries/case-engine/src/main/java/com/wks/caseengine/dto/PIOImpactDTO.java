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
public class PIOImpactDTO {
	
	private String id;
    private String description;
    private Integer startMonth;
    private Integer endMonth;
    private Double value;
    private String remarks;
    private String aopYear;
    private String plantId;
    private String siteId;
    private String verticalId;
    private String saveStatus;
	private String errDescription;

}
