package com.wks.caseengine.dto;

import java.util.Date;


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
public class FinishingShutdownConfigDTO {
	
private String id;
    
    private Integer year;
    
    private Integer month;
    
    private Double shutdownHours;
    
    private Date shutdownDate;
    
    private Integer category;
    
    private String auditYear;
    
    private String remarks;
    
    private Date updatedOn;
    
    private String updatedBy;
    
    private String plantFkId;
}
