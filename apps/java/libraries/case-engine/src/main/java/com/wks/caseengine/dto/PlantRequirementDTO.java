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
public class PlantRequirementDTO {
	private String plantName;  // Process Plant
    private String plantCode;
	private String cppUtilities;  // CPP Utility
    private String cppUtiltiyIds;  // CPP Utility ID
    private String cppPlant;      // CPP Plant
    private String cppPlantId; 
    private String uom;    // UOM
    private Double april;
    private Double may;
    private Double june;
    private Double july;
    private Double aug;
    private Double sep;
    private Double oct;
    private Double nov;
    private Double dec;
    private Double jan;
    private Double feb;
    private Double march;
    private Double grandTotal;  
    private String remarks;
    
    // Fields for import/export tracking
    private String saveStatus;
    private String errDescription;
}
