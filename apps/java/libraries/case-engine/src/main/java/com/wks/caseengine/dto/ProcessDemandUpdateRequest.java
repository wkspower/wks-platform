package com.wks.caseengine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessDemandUpdateRequest {
    
    private String processPlantId;  // Required - composite key part  plantCode
    private String cppUtilityId;    // Required - composite key part
    
    // Optional month fields - only send months being updated
    private Double apr;
    private Double may;
    private Double jun;
    private Double jul;
    private Double aug;
    private Double sep;
    private Double oct;
    private Double nov;
    private Double dec;
    private Double jan;
    private Double feb;
    private Double mar;
    
    private String remarks;  // Required for audit trail
}
